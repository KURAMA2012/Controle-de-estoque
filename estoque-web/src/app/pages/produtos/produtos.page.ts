import { ChangeDetectionStrategy, Component, DestroyRef, WritableSignal, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, debounceTime, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { CadastroService } from '../../core/cadastro.service';
import {
  FiltrosItem, Item, ItemEspecificacao, ItemImagem, ItemService, NivelEstoque, Pagina, Ref, RequestPage,
  nivelEstoque, srcImagem,
} from '../../core/item.service';
import { ToastService, mensagemDeErro } from '../../core/toast.service';
import { VIEW_MODES, ViewModeService } from '../../core/view-mode.service';
import { IconeComponent } from '../../shared/icon.component';
import { ThumbComponent } from '../../shared/thumb.component';
import { ViewSwitcherComponent } from '../../shared/view-switcher.component';

type Status = 'todos' | 'ativos' | 'inativos';
type Ordem = 'nome' | 'valorVenda' | 'quantidadeEstoqueAtual';

/** Limite para os KPIs do modo Detalhe (1 requisição com todos os itens filtrados). */
const LIMITE_KPI = 1000;

const BRL = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

interface Kpis {
  total: number;
  semEstoque: number;
  valor: number;
  parcial: boolean;
}

interface Detalhe {
  carregando: boolean;
  item: Item | null;
  imagens: ItemImagem[];
  especificacoes: ItemEspecificacao[];
  erro: string | null;
}

@Component({
  selector: 'app-produtos',
  imports: [FormsModule, RouterLink, NgTemplateOutlet, ViewSwitcherComponent, IconeComponent, ThumbComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './produtos.page.html',
})
export class ProdutosPage {
  protected readonly vm = inject(ViewModeService);
  private readonly api = inject(ItemService);
  private readonly cadastros = inject(CadastroService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  /** Busca vinda da topbar (?q=) */
  readonly q = input<string>();

  protected readonly atual = computed(() => VIEW_MODES.find((o) => o.valor === this.vm.modo())!);
  protected readonly ordens: { valor: Ordem; rotulo: string }[] = [
    { valor: 'nome', rotulo: 'Nome' },
    { valor: 'valorVenda', rotulo: 'Preço' },
    { valor: 'quantidadeEstoqueAtual', rotulo: 'Estoque' },
  ];
  protected readonly tamanhosPagina = [12, 24, 48];

  // ---- filtros do servidor ----
  protected readonly busca = signal('');
  protected readonly categoria = signal<number | null>(null);
  protected readonly marca = signal<number | null>(null);
  protected readonly status = signal<Status>('todos');
  protected readonly ordem = signal<Ordem>('nome');
  protected readonly direcao = signal<'asc' | 'desc'>('asc');
  protected readonly pagina = signal(0);
  protected readonly tamanho = signal(12);
  private readonly recarga = signal(0);

  // ---- filtros locais (só modo Cards, aplicados à página atual) ----
  protected readonly cor = signal<number | null>(null);
  protected readonly tamanhoProduto = signal<number | null>(null);
  protected readonly precoMin = signal<number | null>(null);
  protected readonly precoMax = signal<number | null>(null);

  // ---- listas auxiliares ----
  protected readonly categorias = signal<Ref[]>([]);
  protected readonly marcas = signal<Ref[]>([]);
  protected readonly cores = signal<Ref[]>([]);
  protected readonly tamanhos = signal<Ref[]>([]);

  // ---- estado ----
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly resultado = signal<Pagina<Item> | null>(null);
  protected readonly kpis = signal<Kpis | null>(null);
  protected readonly selecionadoId = signal<number | null>(null);
  protected readonly detalhe = signal<Detalhe>({ carregando: false, item: null, imagens: [], especificacoes: [], erro: null });
  protected readonly fotoId = signal<number | null>(null);
  protected readonly paraExcluir = signal<Item | null>(null);
  protected readonly excluindo = signal(false);

  protected readonly filtros = computed<FiltrosItem>(() => {
    const f: FiltrosItem = {};
    const b = this.busca().trim();
    if (b) f.nome = b;
    if (this.categoria() != null) f.idCategoria = { id: this.categoria()! };
    if (this.marca() != null) f.idMarca = { id: this.marca()! };
    if (this.status() !== 'todos') f.ativo = this.status() === 'ativos';
    return f;
  });

  protected readonly filtradoNoServidor = computed(() => Object.keys(this.filtros()).length > 0);
  protected readonly filtroLocalAtivo = computed(
    () => this.vm.modo() === 'cards' &&
      (this.cor() != null || this.tamanhoProduto() != null || this.precoMin() != null || this.precoMax() != null),
  );
  protected readonly algumFiltro = computed(() => this.filtradoNoServidor() || this.filtroLocalAtivo());

  protected readonly itens = computed<Item[]>(() => {
    const lista = this.resultado()?.content ?? [];
    if (!this.filtroLocalAtivo()) return lista;
    const cor = this.cor();
    const tam = this.tamanhoProduto();
    const min = this.precoMin();
    const max = this.precoMax();
    return lista.filter((it) => {
      if (cor != null && !(it.cores ?? []).some((c) => c.id === cor)) return false;
      if (tam != null && !(it.tamanhos ?? []).some((t) => t.id === tam)) return false;
      const p = it.valorVenda ?? 0;
      if (min != null && p < min) return false;
      if (max != null && p > max) return false;
      return true;
    });
  });

  protected readonly totalPaginas = computed(() => {
    const r = this.resultado();
    return r ? Math.max(1, r.totalPages) : null;
  });
  protected readonly temProxima = computed(() => {
    const r = this.resultado();
    if (!r) return false;
    const tp = this.totalPaginas();
    return tp != null ? this.pagina() + 1 < tp : r.content.length === this.tamanho();
  });

  protected readonly resumo = computed(() => {
    const r = this.resultado();
    if (!r) return '';
    return `${r.totalElements} produto(s)${this.filtradoNoServidor() ? ' encontrado(s)' : ''}`;
  });

  protected readonly selecionado = computed<Item | null>(() => {
    const lista = this.itens();
    const id = this.selecionadoId();
    return lista.find((i) => i.id === id) ?? lista[0] ?? null;
  });

  protected readonly fotos = computed(() => {
    const d = this.detalhe();
    const imgs = [...d.imagens].sort((a, b) => Number(b.principal) - Number(a.principal) || a.ordem - b.ordem);
    return imgs.map((i) => ({ id: i.id, principal: i.principal, src: srcImagem(i.imagem) ?? i.url }));
  });
  protected readonly fotoAtual = computed(() => {
    const fs = this.fotos();
    return fs.find((f) => f.id === this.fotoId()) ?? fs[0] ?? null;
  });

  constructor() {
    effect(() => {
      const q = this.q() ?? '';
      untracked(() => {
        this.busca.set(q);
        this.pagina.set(0);
      });
    });

    this.carregarAuxiliares();

    const req = computed<RequestPage & { _r: number }>(() => ({
      filtros: this.filtros(),
      orderBy: this.ordem(),
      direcao: this.direcao(),
      page: this.pagina(),
      size: this.tamanho(),
      _r: this.recarga(),
    }));
    toObservable(req)
      .pipe(
        debounceTime(250),
        tap(() => {
          this.carregando.set(true);
          this.erro.set(null);
        }),
        switchMap(({ _r, ...r }) =>
          this.api.consultar(r).pipe(
            catchError((e) => {
              this.erro.set(mensagemDeErro(e, 'Não foi possível carregar os produtos.'));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((r) => {
        this.resultado.set(r);
        this.carregando.set(false);
      });

    // KPIs: só no modo Detalhe; uma consulta com todos os itens filtrados (até LIMITE_KPI).
    const reqKpi = computed(() =>
      this.vm.modo() === 'detalhe' ? { filtros: this.filtros(), page: 0, size: LIMITE_KPI, orderBy: 'nome', _r: this.recarga() } : null,
    );
    toObservable(reqKpi)
      .pipe(
        debounceTime(300),
        switchMap((r) => {
          if (!r) return of(null);
          const { _r, ...corpo } = r;
          return this.api.consultar(corpo).pipe(
            map((p): Kpis => {
              const lista = p.content;
              return {
                total: p.totalElements,
                semEstoque: lista.filter((i) => (i.quantidadeEstoqueAtual ?? 0) <= 0).length,
                valor: lista.reduce((s, i) => s + (i.valorVenda ?? 0) * Math.max(0, i.quantidadeEstoqueAtual ?? 0), 0),
                parcial: lista.length >= LIMITE_KPI,
              };
            }),
            catchError(() => of(null)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((k) => this.kpis.set(k));

    // Detalhe do item selecionado: detalhar + imagens + especificações (imagens/especificações em cache).
    const idDetalhe = computed(() => (this.vm.modo() === 'detalhe' ? (this.selecionado()?.id ?? null) : null));
    toObservable(idDetalhe)
      .pipe(
        tap((id) => {
          this.fotoId.set(null);
          this.detalhe.set({ carregando: id != null, item: null, imagens: [], especificacoes: [], erro: null });
        }),
        switchMap((id) => {
          if (id == null) return of(null);
          return forkJoin({
            item: this.api.detalhar(id),
            imagens: this.api.imagens(id).pipe(catchError(() => of([] as ItemImagem[]))),
            especificacoes: this.api.especificacoes(id).pipe(catchError(() => of([] as ItemEspecificacao[]))),
          }).pipe(
            map((d): Detalhe => ({ ...d, carregando: false, erro: null })),
            catchError((e) => of<Detalhe>({ carregando: false, item: null, imagens: [], especificacoes: [], erro: mensagemDeErro(e, 'Não foi possível carregar o produto.') })),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((d) => d && this.detalhe.set(d));
  }

  private carregarAuxiliares(): void {
    const carregar = (endpoint: string, alvo: WritableSignal<Ref[]>) =>
      this.cadastros.listar<Ref & { ativo: boolean }>(endpoint).subscribe({
        next: (l) => alvo.set([...l].sort((a, b) => a.descricao.localeCompare(b.descricao, 'pt-BR'))),
        error: () => alvo.set([]),
      });
    carregar('categoria', this.categorias);
    carregar('marca', this.marcas);
    carregar('cor', this.cores);
    carregar('tamanho', this.tamanhos);
  }

  // ---- ações ----
  protected alterar<T>(s: WritableSignal<T>, v: T): void {
    s.set(v);
    this.pagina.set(0);
  }

  protected numero(v: unknown): number | null {
    return v === '' || v == null || isNaN(Number(v)) ? null : Number(v);
  }

  /** Mesmo campo: inverte a direção; outro campo: começa em asc. */
  protected ordenar(o: Ordem): void {
    if (this.ordem() === o) this.direcao.update((d) => (d === 'asc' ? 'desc' : 'asc'));
    else this.direcao.set('asc');
    this.alterar(this.ordem, o);
  }

  protected selecionarOrdem(o: Ordem): void {
    this.alterar(this.ordem, o);
  }

  protected alternarDirecao(): void {
    this.alterar(this.direcao, this.direcao() === 'asc' ? 'desc' : 'asc');
  }

  /** Seleciona o produto no modo Detalhe; em telas estreitas (painel abaixo da lista) rola até o painel. */
  protected selecionarDetalhe(id: number): void {
    this.selecionadoId.set(id);
    if (typeof window !== 'undefined' && window.matchMedia('(max-width: 900px)').matches) {
      setTimeout(() => document.querySelector('.painel-detalhe')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
    }
  }

  protected irPara(p: number): void {
    if (p < 0) return;
    this.pagina.set(p);
  }

  protected recarregar(): void {
    this.api.limparCache();
    this.recarga.update((v) => v + 1);
  }

  protected limparFiltros(): void {
    this.busca.set('');
    this.categoria.set(null);
    this.marca.set(null);
    this.status.set('todos');
    this.cor.set(null);
    this.tamanhoProduto.set(null);
    this.precoMin.set(null);
    this.precoMax.set(null);
    this.pagina.set(0);
    if (this.q()) this.router.navigate(['/produtos']);
  }

  /** Ação "Detalhar" (Tabela/Cards): muda para o modo Detalhe com o produto selecionado e rola até o painel. */
  protected detalhar(it: Item): void {
    this.selecionadoId.set(it.id);
    this.vm.definir('detalhe');
    setTimeout(() => document.querySelector('.painel-detalhe')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
  }

  protected editar(it: Item): void {
    this.router.navigate(['/produtos', it.id]);
  }

  protected excluir(): void {
    const it = this.paraExcluir();
    if (!it) return;
    this.excluindo.set(true);
    this.api.deletar(it.id).subscribe({
      next: () => {
        this.excluindo.set(false);
        this.paraExcluir.set(null);
        this.api.limparCache(it.id);
        if (this.selecionadoId() === it.id) this.selecionadoId.set(null);
        this.toast.sucesso(`Produto "${it.nome}" excluído.`);
        if (this.itens().length === 1 && this.pagina() > 0) this.pagina.update((p) => p - 1);
        this.recarga.update((v) => v + 1);
      },
      error: (e) => {
        this.excluindo.set(false);
        this.paraExcluir.set(null);
        this.toast.erroApi(e, 'Não foi possível excluir o produto.');
      },
    });
  }

  // ---- formatação ----
  protected preco(v?: number | null): string {
    return v == null ? '—' : BRL.format(v);
  }

  protected nivel(it: Item): NivelEstoque {
    return nivelEstoque(it);
  }

  protected rotuloEstoque(it: Item): string {
    const q = it.quantidadeEstoqueAtual ?? 0;
    const un = it.unidade?.descricao ? ` ${it.unidade.descricao}` : '';
    if (q <= 0) return 'Sem estoque';
    return `${q}${un}${this.nivel(it) === 'baixo' ? ' · baixo' : ''}`;
  }
}
