import { ChangeDetectionStrategy, Component, DestroyRef, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ThemeService } from '../core/theme.service';
import { IconeComponent } from '../shared/icon.component';
import { ToastsComponent } from '../shared/toast.component';

interface ItemMenu {
  rota: string;
  rotulo: string;
  icone: string;
}

const CHAVE_SIDEBAR = 'estoque.sidebarRecolhida';
const MQ_MOVEL = '(max-width: 900px)';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule, IconeComponent, ToastsComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './shell.component.html',
})
export class ShellComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly tema = inject(ThemeService);
  private readonly router = inject(Router);

  protected readonly recolhida = signal(localStorage.getItem(CHAVE_SIDEBAR) === '1');
  protected readonly menuUsuarioAberto = signal(false);
  /** Abaixo de 900px a sidebar vira drawer off-canvas. */
  protected readonly movel = signal(window.matchMedia(MQ_MOVEL).matches);
  protected readonly menuMovel = signal(false);
  protected busca = '';

  protected readonly principais: ItemMenu[] = [
    { rota: '/dashboard', rotulo: 'Dashboard', icone: 'dashboard' },
    { rota: '/produtos', rotulo: 'Produtos', icone: 'produtos' },
  ];

  protected readonly cadastros: ItemMenu[] = [
    { rota: '/cadastros/categorias', rotulo: 'Categorias', icone: 'categorias' },
    { rota: '/cadastros/marcas', rotulo: 'Marcas', icone: 'marcas' },
    { rota: '/cadastros/modelos', rotulo: 'Modelos', icone: 'modelos' },
    { rota: '/cadastros/unidades', rotulo: 'Unidades', icone: 'unidades' },
    { rota: '/cadastros/cores', rotulo: 'Cores', icone: 'cores' },
    { rota: '/cadastros/tamanhos', rotulo: 'Tamanhos', icone: 'tamanhos' },
  ];

  protected readonly iniciais = computed(() => {
    const n = this.auth.usuario()?.nome || this.auth.usuario()?.login || '?';
    return n
      .split(/\s+/)
      .slice(0, 2)
      .map((p) => p[0]?.toUpperCase() ?? '')
      .join('');
  });

  constructor() {
    const mq = window.matchMedia(MQ_MOVEL);
    const ouvir = (e: MediaQueryListEvent) => {
      this.movel.set(e.matches);
      if (!e.matches) this.menuMovel.set(false);
    };
    mq.addEventListener('change', ouvir);
    inject(DestroyRef).onDestroy(() => mq.removeEventListener('change', ouvir));
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd), takeUntilDestroyed())
      .subscribe(() => this.menuMovel.set(false));
  }

  ngOnInit(): void {
    this.auth.carregarUsuario();
  }

  fecharMenuMovel(): void {
    this.menuMovel.set(false);
  }

  @HostListener('document:keydown.escape')
  aoEscape(): void {
    this.menuMovel.set(false);
  }

  alternarSidebar(): void {
    if (this.movel()) {
      this.menuMovel.update((v) => !v);
      return;
    }
    this.recolhida.update((v) => !v);
    localStorage.setItem(CHAVE_SIDEBAR, this.recolhida() ? '1' : '0');
  }

  buscar(): void {
    const q = this.busca.trim();
    this.router.navigate(['/produtos'], { queryParams: q ? { q } : {} });
  }

  @HostListener('document:click', ['$event'])
  fecharMenu(ev: MouseEvent): void {
    const alvo = ev.target as HTMLElement;
    if (!alvo.closest('.menu-usuario')) this.menuUsuarioAberto.set(false);
  }
}
