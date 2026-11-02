import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from '../core/toast.service';
import { IconeComponent } from './icon.component';

@Component({
  selector: 'app-toasts',
  imports: [IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toasts" aria-live="polite">
      @for (t of svc.toasts(); track t.id) {
        <div class="toast" [class]="'toast toast-' + t.tipo" role="status">
          <app-icone [nome]="t.tipo === 'erro' ? 'alerta' : t.tipo === 'sucesso' ? 'check' : 'info'" [tamanho]="18" />
          <span>{{ t.mensagem }}</span>
          <button type="button" class="btn-icone pequeno" (click)="svc.fechar(t.id)" aria-label="Fechar">
            <app-icone nome="fechar" [tamanho]="16" />
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastsComponent {
  protected readonly svc = inject(ToastService);
}
