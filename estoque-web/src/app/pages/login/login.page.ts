import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { environment } from '../../../environments/environment';
import { ThemeService } from '../../core/theme.service';
import { IconeComponent } from '../../shared/icon.component';

@Component({
  selector: 'app-login',
  imports: [FormsModule, IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="login-pagina">
      <button class="btn-icone tema-login" type="button" (click)="tema.alternar()" aria-label="Alternar tema">
        <app-icone [nome]="tema.tema() === 'escuro' ? 'sol' : 'lua'" />
      </button>
      <form class="cartao login-cartao" (ngSubmit)="entrar()" #f="ngForm">
        <div class="login-marca">
          <span class="logo"><app-icone nome="caixa" [tamanho]="28" /></span>
          <h1>Estoque</h1>
          <p>Entre para gerenciar seus produtos</p>
        </div>

        <label class="campo">
          <span>Usuário</span>
          <input name="login" [(ngModel)]="login" required autocomplete="username" autofocus />
        </label>
        <label class="campo">
          <span>Senha</span>
          <input name="senha" type="password" [(ngModel)]="senha" required autocomplete="current-password" />
        </label>

        @if (erro()) {
          <div class="erro" role="alert">{{ erro() }}</div>
        }

        <button class="btn btn-primario" type="submit" [disabled]="carregando() || f.invalid">
          {{ carregando() ? 'Entrando...' : 'Entrar' }}
        </button>
        @if (demo) {
          <div class="login-demo">
            <span>Demonstração: usuário <strong>{{ demo.login }}</strong> / senha <strong>{{ demo.senha }}</strong></span>
            <button class="btn btn-secundario" type="button" data-acao="entrar-demo" (click)="entrarDemo()" [disabled]="carregando()">Entrar como demo</button>
          </div>
        }
      </form>
    </div>
  `,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly tema = inject(ThemeService);

  protected login = '';
  protected senha = '';
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');

  protected readonly demo = environment.demoLogin ? { login: environment.demoLogin, senha: environment.demoSenha } : null;

  entrarDemo(): void {
    if (!this.demo) return;
    this.login = this.demo.login;
    this.senha = this.demo.senha;
    this.entrar();
  }

  entrar(): void {
    this.erro.set('');
    this.carregando.set(true);
    this.auth.login(this.login.trim(), this.senha).subscribe({
      next: () => this.router.navigate(['/']),
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set(
          e.status === 401 ? 'Usuário ou senha inválidos.' : e.status === 0 ? 'Não foi possível conectar à API.' : 'Erro ao entrar. Tente novamente.',
        );
      },
    });
  }
}
