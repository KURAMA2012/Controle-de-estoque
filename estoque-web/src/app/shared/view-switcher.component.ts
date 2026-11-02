import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { VIEW_MODES, ViewModeService } from '../core/view-mode.service';
import { IconeComponent } from './icon.component';

@Component({
  selector: 'app-view-switcher',
  imports: [IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="segmentado" role="radiogroup" aria-label="Modo de visualização">
      @for (o of opcoes; track o.valor) {
        <button type="button" role="radio" [attr.aria-checked]="vm.modo() === o.valor"
          [class.ativo]="vm.modo() === o.valor" [title]="o.descricao" (click)="vm.definir(o.valor)">
          <app-icone [nome]="o.icone" [tamanho]="18" />
          <span>{{ o.rotulo }}</span>
        </button>
      }
    </div>
  `,
})
export class ViewSwitcherComponent {
  protected readonly vm = inject(ViewModeService);
  protected readonly opcoes = VIEW_MODES;
}
