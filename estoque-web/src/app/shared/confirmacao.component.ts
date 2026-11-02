import { ChangeDetectionStrategy, Component, ElementRef, HostListener, effect, inject, viewChild } from '@angular/core';
import { ConfirmacaoService } from '../core/confirmacao.service';
import { IconeComponent } from './icon.component';

@Component({
  selector: 'app-confirmacao',
  imports: [IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (svc.atual(); as p) {
      <div class="overlay" (click)="svc.responder(false)"></div>
      <div class="modal" role="alertdialog" aria-modal="true" aria-labelledby="titulo-confirmacao" aria-describedby="texto-confirmacao">
        <div class="modal-icone"><app-icone [nome]="p.perigo ? 'lixeira' : 'info'" [tamanho]="22" /></div>
        <h2 id="titulo-confirmacao">{{ p.titulo }}</h2>
        <p id="texto-confirmacao">{{ p.mensagem }}</p>
        <div class="modal-acoes">
          <button class="btn btn-secundario" type="button" data-acao="cancelar-confirmacao" (click)="svc.responder(false)">{{ p.cancelar || 'Cancelar' }}</button>
          <button #ok class="btn" [class.btn-perigo]="p.perigo" [class.btn-primario]="!p.perigo" type="button"
            data-acao="confirmar" (click)="svc.responder(true)">{{ p.confirmar || 'Confirmar' }}</button>
        </div>
      </div>
    }
  `,
})
export class ConfirmacaoComponent {
  protected readonly svc = inject(ConfirmacaoService);
  private readonly ok = viewChild<ElementRef<HTMLButtonElement>>('ok');

  constructor() {
    effect(() => this.ok()?.nativeElement.focus());
  }

  @HostListener('document:keydown.escape')
  protected esc(): void {
    this.svc.responder(false);
  }
}
