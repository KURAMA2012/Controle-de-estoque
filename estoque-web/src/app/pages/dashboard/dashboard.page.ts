import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="cabecalho-pagina">
      <div>
        <h1>Dashboard</h1>
        <p class="sub">Olá, {{ auth.usuario()?.nome || auth.usuario()?.login }}. Os indicadores chegam nas próximas etapas.</p>
      </div>
    </div>
    <div class="cartao vazio">Em construção.</div>
  `,
})
export class DashboardPage {
  protected readonly auth = inject(AuthService);
}
