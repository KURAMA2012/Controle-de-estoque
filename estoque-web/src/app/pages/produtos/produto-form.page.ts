import { ChangeDetectionStrategy, Component, WritableSignal, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CadastroService, Modelo } from '../../core/cadastro.service';
import { ImagemPrincipalService } from '../../core/imagem-principal.service';
import { Item, ItemEspecificacao, ItemImagem, ItemRequest, ItemService, Ref, srcImagem } from '../../core/item.service';
import { ToastService } from '../../core/toast.service';
import { ConfirmacaoService } from '../../core/confirmacao.service';
import { IconeComponent } from '../../shared/icon.component';

type Aba = 'cadastro' | 'imagens' | 'especificacoes';
const LIMITE_IMAGENS = 10;
const TIPOS_IMAGEM = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];

function vazio(): ItemRequest {
  return {
    nome: '', idUnidade: null, idCategoria: null, idMarca: null, idModelo: null, codigoBarras: '', icone: '',
    descricao: '', dataValidade: null, quantidadeEstoqueMinimo: null, quantidadeEstoqueMaximo: null,
    quantidadeEstoqueAtual: 0, ativo: true, valorCusto: null, margemLucro: null, valorVenda: null, precoAntigo: null,
    maxParcelas: null, full: false, freteGratis: false, rating: null, qtdReviews: null, qtdVendidos: null,
    codigoFornecedor: '', idsCores: [], idsTamanhos: [],
  };
}

function paraRequest(it: Item): ItemRequest {
  return {
    id: it.id, nome: it.nome ?? '', idUnidade: it.unidade?.id ?? null, idCategoria: it.idCategoria?.id ?? null,
    idMarca: it.idMarca?.id ?? null, idModelo: it.idModelo?.id ?? null, codigoBarras: it.codigoBarras ?? '',
    icone: it.icone ?? '', descricao: it.descricao ?? '', dataValidade: it.data_validade ?? null,
    quantidadeEstoqueMinimo: it.quantidadeEstoqueMinimo ?? null, quantidadeEstoqueMaximo: it.quantidadeEstoqueMaximo ?? null,
    quantidadeEstoqueAtual: it.quantidadeEstoqueAtual ?? null, ativo: !!it.ativo, valorCusto: it.valorCusto ?? null,
    margemLucro: it.margemLucro ?? null, valorVenda: it.valorVenda ?? null, precoAntigo: it.precoAntigo ?? null,
    maxParcelas: it.maxParcelas ?? null, full: !!it.full, freteGratis: !!it.freteGratis, rating: it.rating ?? null,
    qtdReviews: it.qtdReviews ?? null, qtdVendidos: it.qtdVendidos ?? null, codigoFornecedor: it.codigoFornecedor ?? '',
    idsCores: (it.cores ?? []).map((c) => c.id), idsTamanhos: (it.tamanhos ?? []).map((t) => t.id),
  };
}

const ordenar = (l: Ref[]) => [...l].sort((a, b) => a.descricao.localeCompare(b.descricao, 'pt-BR'));

@Component({
  selector: 'app-produto-form',
  imports: [FormsModule, RouterLink, IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './produto-form.page.html',
})
export class ProdutoFormPage {
  private readonly api = inject(ItemService);
  private readonly cad = inject(CadastroService);
  private readonly toast = inject(ToastService);
  private readonly confirmacao = inject(ConfirmacaoService);
  private readonly router = inject(Router);
  private readonly thumbs = inject(ImagemPrincipalService);

  /** Rota /produtos/:id (ausente em /produtos/novo). */
  readonly id = input<string>();
  protected readonly idItem = computed(() => {
    const n = Number(this.id());
    return this.id() && !isNaN(n) ? n : null;
  });
  protected readonly salvo = computed(() => this.idItem() != null);
  protected readonly limite = LIMITE_IMAGENS;

  protected readonly aba = signal<Aba>('cadastro');
  protected readonly form = signal<ItemRequest>(vazio());
  protected readonly enviado = signal(false);
  protected readonly carregando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erroCarga = signal<string | null>(null);

  protected readonly unidades = signal<Ref[]>([]);
  protected readonly categorias = signal<Ref[]>([]);
  protected readonly marcas = signal<Ref[]>([]);
  protected readonly modelos = signal<Ref[]>([]);
  protected readonly cores = signal<Ref[]>([]);
  protected readonly tamanhos = signal<Ref[]>([]);

  protected readonly imagens = signal<ItemImagem[]>([]);
  protected readonly ocupadoImagens = signal(false);
  protected readonly urlNova = signal('');
  protected readonly paraRemover = signal<ItemImagem | null>(null);

  protected readonly specs = signal<ItemEspecificacao[]>([]);
  protected readonly novaChave = signal('');
  protected readonly novoValor = signal('');
  protected readonly editandoSpec = signal<number | null>(null);
  protected readonly editChave = signal('');
  protected readonly editValor = signal('');

  protected readonly erros = computed(() => {
    const f = this.form();
    const e: Record<string, string> = {};
    if (!f.nome?.trim()) e['nome'] = 'Informe o nome.';
    else if (f.nome.trim().length > 255) e['nome'] = 'Máximo de 255 caracteres.';
    for (const k of ['quantidadeEstoqueMinimo', 'quantidadeEstoqueMaximo', 'quantidadeEstoqueAtual', 'maxParcelas', 'qtdReviews', 'qtdVendidos'] as const) {
      const v = f[k];
      if (v != null && (v < 0 || !Number.isInteger(v))) e[k] = 'Use um número inteiro ≥ 0.';
    }
    for (const k of ['valorCusto', 'margemLucro', 'valorVenda', 'precoAntigo'] as const) {
      const v = f[k];
      if (v != null && v < 0) e[k] = 'Não pode ser negativo.';
    }
    if (f.quantidadeEstoqueMinimo != null && f.quantidadeEstoqueMaximo != null && f.quantidadeEstoqueMinimo > f.quantidadeEstoqueMaximo)
      e['quantidadeEstoqueMaximo'] = 'O máximo deve ser ≥ mínimo.';
    if (f.rating != null && (f.rating < 0 || f.rating > 5)) e['rating'] = 'Entre 0 e 5.';
    return e;
  });
  protected readonly valido = computed(() => Object.keys(this.erros()).length === 0);
  protected readonly principal = computed(() => this.imagens().find((i) => i.principal) ?? null);
  protected readonly galeria = computed(() =>
    [...this.imagens()]
      .sort((a, b) => Number(b.principal) - Number(a.principal) || a.ordem - b.ordem)
      .map((i) => ({ ...i, src: srcImagem(i.imagem) ?? i.url })),
  );
  protected readonly cheio = computed(() => this.imagens().length >= LIMITE_IMAGENS);

  constructor() {
    const lista = (ep: string, alvo: WritableSignal<Ref[]>) =>
      this.cad.listar<Ref & { ativo: boolean }>(ep).subscribe({ next: (l) => alvo.set(ordenar(l)), error: () => alvo.set([]) });
    lista('unidade', this.unidades);
    lista('categoria', this.categorias);
    lista('marca', this.marcas);
    lista('cor', this.cores);
    lista('tamanho', this.tamanhos);

    effect(() => {
      const id = this.idItem();
      untracked(() => this.carregar(id));
    });
  }

  private carregar(id: number | null): void {
    this.enviado.set(false);
    this.erroCarga.set(null);
    if (id == null) {
      this.form.set(vazio());
      this.modelos.set([]);
      this.imagens.set([]);
      this.specs.set([]);
      this.aba.set('cadastro');
      return;
    }
    this.carregando.set(true);
    this.api.detalhar(id).subscribe({
      next: (it) => {
        const f = paraRequest(it);
        this.form.set(f);
        this.carregarModelos(f.idMarca ?? null, it.idModelo ? [it.idModelo] : []);
        this.carregando.set(false);
        this.carregarImagens();
        this.carregarSpecs();
      },
      error: (e) => {
        this.carregando.set(false);
        this.erroCarga.set('Não foi possível carregar o produto.');
        this.toast.erroApi(e, 'Não foi possível carregar o produto.');
      },
    });
  }

  // ---------- Cadastro ----------
  protected set<K extends keyof ItemRequest>(campo: K, valor: ItemRequest[K]): void {
    this.form.update((f) => ({ ...f, [campo]: valor }));
  }

  protected num(v: unknown): number | null {
    return v === '' || v == null || isNaN(Number(v)) ? null : Number(v);
  }

  protected texto(v: unknown): string {
    return v == null ? '' : String(v);
  }

  protected erro(campo: string): string | null {
    return this.enviado() ? (this.erros()[campo] ?? null) : null;
  }

  protected trocarMarca(idMarca: number | null): void {
    this.form.update((f) => ({ ...f, idMarca, idModelo: null }));
    this.carregarModelos(idMarca, []);
  }

  private carregarModelos(idMarca: number | null, garantir: Ref[]): void {
    if (idMarca == null) {
      this.modelos.set(garantir);
      return;
    }
    this.cad.listarModelosPorMarca(idMarca).subscribe({
      next: (l: Modelo[]) => {
        const refs: Ref[] = l.map((m) => ({ id: m.id!, descricao: m.descricao, ativo: m.ativo }));
        for (const g of garantir) if (!refs.some((r) => r.id === g.id)) refs.push(g);
        this.modelos.set(ordenar(refs));
      },
      error: () => this.modelos.set(garantir),
    });
  }

  protected alternarChip(campo: 'idsCores' | 'idsTamanhos', id: number): void {
    this.form.update((f) => {
      const atual = f[campo];
      return { ...f, [campo]: atual.includes(id) ? atual.filter((x) => x !== id) : [...atual, id] };
    });
  }

  protected salvar(): void {
    this.enviado.set(true);
    if (!this.valido()) {
      this.toast.erro('Corrija os campos destacados.');
      return;
    }
    const f = this.form();
    const corpo: ItemRequest = {
      ...f,
      nome: f.nome.trim(),
      codigoBarras: f.codigoBarras?.trim() || null,
      codigoFornecedor: f.codigoFornecedor?.trim() || null,
      icone: f.icone?.trim() || null,
      descricao: f.descricao?.trim() || null,
      dataValidade: f.dataValidade || null,
    };
    const id = this.idItem();
    this.salvando.set(true);
    const req = id == null ? this.api.inserir(corpo) : this.api.atualizar(id, { ...corpo, id });
    req.subscribe({
      next: (it) => {
        this.salvando.set(false);
        this.api.limparCache(it.id);
        if (id == null) {
          this.toast.sucesso(`Produto "${it.nome}" cadastrado. Agora você pode adicionar imagens e especificações.`);
          this.router.navigate(['/produtos', it.id], { replaceUrl: true });
        } else {
          this.toast.sucesso(`Produto "${it.nome}" atualizado.`);
          this.form.set(paraRequest(it));
          this.enviado.set(false);
        }
      },
      error: (e) => {
        this.salvando.set(false);
        this.toast.erroApi(e, 'Não foi possível salvar o produto.');
      },
    });
  }

  // ---------- Imagens ----------
  private carregarImagens(): void {
    const id = this.idItem();
    if (id == null) return;
    this.api.limparCache(id);
    this.api.imagens(id).subscribe({ next: (l) => this.imagens.set(l), error: () => this.imagens.set([]) });
  }

  private aposAlterarImagens(msg: string): void {
    const id = this.idItem()!;
    this.thumbs.invalidar(id);
    this.ocupadoImagens.set(false);
    this.toast.sucesso(msg);
    this.carregarImagens();
  }

  private falhaImagens(e: unknown, padrao: string): void {
    this.ocupadoImagens.set(false);
    this.toast.erroApi(e, padrao);
  }

  protected enviarArquivos(ev: Event): void {
    const inp = ev.target as HTMLInputElement;
    const arquivos = Array.from(inp.files ?? []);
    inp.value = '';
    const id = this.idItem();
    if (id == null || arquivos.length === 0) return;
    const livres = LIMITE_IMAGENS - this.imagens().length;
    if (livres <= 0) {
      this.toast.erro(`Limite de ${LIMITE_IMAGENS} imagens atingido.`);
      return;
    }
    const validos = arquivos.filter((a) => TIPOS_IMAGEM.includes(a.type));
    if (validos.length < arquivos.length) this.toast.erro('Alguns arquivos foram ignorados: use PNG, JPEG, GIF ou WEBP.');
    const envio = validos.slice(0, livres);
    if (validos.length > livres) this.toast.info(`Apenas ${livres} imagem(ns) enviada(s): limite de ${LIMITE_IMAGENS}.`);
    if (!envio.length) return;
    this.ocupadoImagens.set(true);
    forkJoin(envio.map((a) => this.api.anexarImagem(id, a))).subscribe({
      next: () => this.aposAlterarImagens(`${envio.length} imagem(ns) enviada(s).`),
      error: (e) => {
        this.falhaImagens(e, 'Não foi possível enviar a imagem.');
        this.carregarImagens();
      },
    });
  }

  protected adicionarUrl(): void {
    const id = this.idItem();
    const url = this.urlNova().trim();
    if (id == null) return;
    if (!/^https?:\/\/\S+$/i.test(url)) {
      this.toast.erro('Informe uma URL http(s) válida.');
      return;
    }
    if (this.cheio()) {
      this.toast.erro(`Limite de ${LIMITE_IMAGENS} imagens atingido.`);
      return;
    }
    this.ocupadoImagens.set(true);
    this.api.adicionarImagemUrl(id, url).subscribe({
      next: () => {
        this.urlNova.set('');
        this.aposAlterarImagens('Imagem importada da URL.');
      },
      error: (e) => this.falhaImagens(e, 'Não foi possível importar a imagem da URL.'),
    });
  }

  protected definirPrincipal(img: ItemImagem): void {
    this.ocupadoImagens.set(true);
    this.api.definirPrincipal(img.id).subscribe({
      next: () => this.aposAlterarImagens('Imagem principal definida.'),
      error: (e) => this.falhaImagens(e, 'Não foi possível definir a imagem principal.'),
    });
  }

  protected confirmarRemocao(): void {
    const img = this.paraRemover();
    if (!img) return;
    this.ocupadoImagens.set(true);
    this.api.removerImagem(img.id).subscribe({
      next: () => {
        this.paraRemover.set(null);
        this.aposAlterarImagens('Imagem removida.');
      },
      error: (e) => {
        this.paraRemover.set(null);
        this.falhaImagens(e, 'Não foi possível remover a imagem.');
      },
    });
  }

  protected async copiar(url: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(url);
      this.toast.sucesso('URL copiada.');
    } catch {
      this.toast.erro('Não foi possível copiar a URL.');
    }
  }

  // ---------- Especificações ----------
  private carregarSpecs(): void {
    const id = this.idItem();
    if (id == null) return;
    this.api.limparCache(id);
    this.api.especificacoes(id).subscribe({ next: (l) => this.specs.set(l), error: () => this.specs.set([]) });
  }

  protected adicionarSpec(): void {
    const id = this.idItem();
    const chave = this.novaChave().trim();
    if (id == null) return;
    if (!chave) {
      this.toast.erro('Informe o nome da especificação.');
      return;
    }
    if (this.specs().some((s) => s.chave.toLowerCase() === chave.toLowerCase())) {
      this.toast.erro(`Já existe a especificação "${chave}".`);
      return;
    }
    this.api.adicionarEspecificacao(id, chave, this.novoValor().trim() || null).subscribe({
      next: () => {
        this.novaChave.set('');
        this.novoValor.set('');
        this.toast.sucesso('Especificação adicionada.');
        this.carregarSpecs();
      },
      error: (e) => this.toast.erroApi(e, 'Não foi possível adicionar a especificação.'),
    });
  }

  protected editarSpec(s: ItemEspecificacao): void {
    this.editandoSpec.set(s.id);
    this.editChave.set(s.chave);
    this.editValor.set(s.valor ?? '');
  }

  protected salvarSpec(s: ItemEspecificacao): void {
    const chave = this.editChave().trim();
    if (!chave) {
      this.toast.erro('Informe o nome da especificação.');
      return;
    }
    this.api.atualizarEspecificacao(s.id, chave, this.editValor().trim() || null).subscribe({
      next: () => {
        this.editandoSpec.set(null);
        this.toast.sucesso('Especificação atualizada.');
        this.carregarSpecs();
      },
      error: (e) => this.toast.erroApi(e, 'Não foi possível atualizar a especificação.'),
    });
  }

  protected async removerSpec(s: ItemEspecificacao): Promise<void> {
    const ok = await this.confirmacao.perguntar({
      titulo: 'Remover especificação?',
      mensagem: `A especificação "${s.chave}" será removida do produto.`,
      confirmar: 'Remover',
    });
    if (!ok) return;
    this.api.removerEspecificacao(s.id).subscribe({
      next: () => {
        this.toast.sucesso('Especificação removida.');
        this.carregarSpecs();
      },
      error: (e) => this.toast.erroApi(e, 'Não foi possível remover a especificação.'),
    });
  }
}
