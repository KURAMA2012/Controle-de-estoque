import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CadastroAuxiliar, CadastroService, Marca, Modelo } from '../../core/cadastro.service';
import { ToastService, mensagemDeErro } from '../../core/toast.service';
import { IconeComponent } from '../../shared/icon.component';
import { CADASTROS, CadastroConfig, TipoCadastro } from './cadastro-config';

type Registro = CadastroAuxiliar & Partial<Pick<Modelo, 'marca'>>;
type FiltroStatus = 'todos' | 'ativos' | 'inativos';

interface Formulario {
  id?: number;
  descricao: string;
  ativo: boolean;
  idMarca: number | null;
}

/**
 * Tela genérica dos cadastros auxiliares, configurada por tipo (rota data.tipo).
 * Modelo é especializado via config.usaMarca: coluna/filtro de marca e select no formulário.
 */
@Component({
  selector: 'app-cadastro',
  imports: [FormsModule, IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cadastro.page.html',
})
export class CadastroPage {
  private readonly api = inject(CadastroService);
  private readonly toast = inject(ToastService);

  readonly tipo = input.required<TipoCadastro>();
  protected readonly cfg = computed<CadastroConfig>(() => CADASTROS[this.tipo()]);

  protected readonly registros = signal<Registro[]>([]);
  protected readonly marcas = signal<Marca[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erroCarga = signal('');

  protected readonly busca = signal('');
  protected readonly filtroStatus = signal<FiltroStatus>('todos');
  protected readonly filtroMarca = signal<number | null>(null);

  protected readonly drawerAberto = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erroForm = signal('');
  protected form: Formulario = this.vazio();

  protected readonly paraExcluir = signal<Registro | null>(null);
  protected readonly excluindo = signal(false);
  protected readonly alternando = signal<number | null>(null);

  protected readonly filtrados = computed(() => {
    const q = normalizar(this.busca());
    const st = this.filtroStatus();
    const m = this.filtroMarca();
    return this.registros()
      .filter((r) => (st === 'todos' ? true : st === 'ativos' ? r.ativo : !r.ativo))
      .filter((r) => (m == null ? true : r.marca?.id === m))
      .filter((r) => !q || normalizar(r.descricao).includes(q) || normalizar(r.marca?.descricao ?? '').includes(q))
      .sort((a, b) => a.descricao.localeCompare(b.descricao, 'pt-BR'));
  });

  protected readonly totais = computed(() => {
    const l = this.registros();
    const ativos = l.filter((r) => r.ativo).length;
    return { total: l.length, ativos, inativos: l.length - ativos };
  });

  protected readonly filtrosAtivos = computed(
    () => !!this.busca() || this.filtroStatus() !== 'todos' || this.filtroMarca() != null,
  );

  constructor() {
    // recarrega ao trocar de tipo (a mesma instância pode ser reaproveitada pelo router)
    effect(() => {
      this.tipo();
      untracked(() => {
        this.busca.set('');
        this.filtroStatus.set('todos');
        this.filtroMarca.set(null);
        this.drawerAberto.set(false);
        this.carregar();
      });
    });
  }

  carregar(): void {
    const cfg = this.cfg();
    this.carregando.set(true);
    this.erroCarga.set('');
    this.api
      .listar<Registro>(cfg.endpoint)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (l) => this.registros.set(l ?? []),
        error: (e) => {
          this.registros.set([]);
          this.erroCarga.set(mensagemDeErro(e, `Não foi possível carregar ${cfg.titulo.toLowerCase()}.`));
        },
      });
    if (cfg.usaMarca) {
      this.api.listar<Marca>('marca').subscribe({
        next: (l) => this.marcas.set((l ?? []).sort((a, b) => a.descricao.localeCompare(b.descricao, 'pt-BR'))),
        error: (e) => this.toast.erroApi(e, 'Não foi possível carregar as marcas.'),
      });
    }
  }

  limparFiltros(): void {
    this.busca.set('');
    this.filtroStatus.set('todos');
    this.filtroMarca.set(null);
  }

  novo(): void {
    this.form = this.vazio();
    if (this.cfg().usaMarca && this.filtroMarca() != null) this.form.idMarca = this.filtroMarca();
    this.erroForm.set('');
    this.drawerAberto.set(true);
  }

  editar(r: Registro): void {
    this.form = { id: r.id, descricao: r.descricao, ativo: r.ativo, idMarca: r.marca?.id ?? null };
    this.erroForm.set('');
    this.drawerAberto.set(true);
  }

  fecharDrawer(): void {
    if (!this.salvando()) this.drawerAberto.set(false);
  }

  salvar(): void {
    const cfg = this.cfg();
    const descricao = this.form.descricao.trim();
    if (!descricao) {
      this.erroForm.set('Informe a descrição.');
      return;
    }
    const corpo: Registro = { descricao, ativo: this.form.ativo };
    if (cfg.usaMarca) corpo.marca = this.form.idMarca != null ? { id: this.form.idMarca } : null;
    if (this.form.id) corpo.id = this.form.id;

    const req = this.form.id
      ? this.api.atualizar<Registro>(cfg.endpoint, this.form.id, corpo)
      : this.api.inserir<Registro>(cfg.endpoint, corpo);

    this.salvando.set(true);
    this.erroForm.set('');
    req.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: (salvo) => {
        this.substituir(this.completarMarca(salvo), !this.form.id);
        this.toast.sucesso(`${capitalizar(cfg.singular)} ${this.form.id ? 'atualizad' : 'cadastrad'}${this.genero()}.`);
        this.drawerAberto.set(false);
      },
      error: (e) => {
        const msg = mensagemDeErro(e, 'Não foi possível salvar.');
        this.erroForm.set(msg);
        this.toast.erro(msg);
      },
    });
  }

  alternarAtivo(r: Registro): void {
    if (!r.id || this.alternando() != null) return;
    const cfg = this.cfg();
    const corpo: Registro = { ...r, ativo: !r.ativo };
    if (cfg.usaMarca) corpo.marca = r.marca ? { id: r.marca.id } : null;
    this.alternando.set(r.id);
    this.api
      .atualizar<Registro>(cfg.endpoint, r.id, corpo)
      .pipe(finalize(() => this.alternando.set(null)))
      .subscribe({
        next: (salvo) => {
          this.substituir(this.completarMarca(salvo), false);
          this.toast.sucesso(`"${salvo.descricao}" ${salvo.ativo ? 'ativad' : 'inativad'}${this.genero()}.`);
        },
        error: (e) => this.toast.erroApi(e, 'Não foi possível alterar o status.'),
      });
  }

  confirmarExclusao(r: Registro): void {
    this.paraExcluir.set(r);
  }

  excluir(): void {
    const r = this.paraExcluir();
    if (!r?.id) return;
    this.excluindo.set(true);
    this.api
      .deletar(this.cfg().endpoint, r.id)
      .pipe(finalize(() => this.excluindo.set(false)))
      .subscribe({
        next: () => {
          this.registros.update((l) => l.filter((x) => x.id !== r.id));
          this.toast.sucesso(`"${r.descricao}" excluíd${this.genero()}.`);
          this.paraExcluir.set(null);
        },
        error: (e) => {
          this.toast.erroApi(e, 'Não foi possível excluir.');
          this.paraExcluir.set(null);
        },
      });
  }

  protected nomeMarca(id: number | null | undefined): string {
    return this.marcas().find((m) => m.id === id)?.descricao ?? '';
  }

  private completarMarca(r: Registro): Registro {
    if (this.cfg().usaMarca && r.marca && !r.marca.descricao) {
      return { ...r, marca: { id: r.marca.id, descricao: this.nomeMarca(r.marca.id) } };
    }
    return r;
  }

  private substituir(r: Registro, novo: boolean): void {
    this.registros.update((l) => (novo ? [...l, r] : l.map((x) => (x.id === r.id ? r : x))));
  }

  private genero(): 'a' | 'o' {
    return this.cfg().novo.startsWith('Nova') ? 'a' : 'o';
  }

  private vazio(): Formulario {
    return { descricao: '', ativo: true, idMarca: null };
  }
}

function normalizar(s: string): string {
  return (s ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
}

function capitalizar(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1);
}
