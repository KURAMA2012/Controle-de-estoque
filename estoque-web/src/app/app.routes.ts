import { Routes } from '@angular/router';
import { authGuard, convidadoGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [convidadoGuard],
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
    title: 'Entrar | Estoque',
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.page').then((m) => m.DashboardPage),
        title: 'Dashboard | Estoque',
      },
      {
        path: 'produtos',
        loadComponent: () => import('./pages/produtos/produtos.page').then((m) => m.ProdutosPage),
        title: 'Produtos | Estoque',
      },
      {
        path: 'produtos/novo',
        loadComponent: () => import('./pages/produtos/produto-form.page').then((m) => m.ProdutoFormPage),
        title: 'Novo produto | Estoque',
      },
      {
        path: 'produtos/:id',
        loadComponent: () => import('./pages/produtos/produto-form.page').then((m) => m.ProdutoFormPage),
        title: 'Editar produto | Estoque',
      },
      ...(
        [
          ['categorias', 'Categorias'],
          ['marcas', 'Marcas'],
          ['modelos', 'Modelos'],
          ['unidades', 'Unidades'],
          ['cores', 'Cores'],
          ['tamanhos', 'Tamanhos'],
        ] as const
      ).map(([path, titulo]) => ({
        path: `cadastros/${path}`,
        loadComponent: () => import('./pages/cadastro/cadastro.page').then((m) => m.CadastroPage),
        data: { tipo: path },
        title: `${titulo} | Estoque`,
      })),
    ],
  },
  { path: '**', redirectTo: '' },
];
