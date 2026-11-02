import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { ImagemPrincipalService } from '../core/imagem-principal.service';
import { IconeComponent } from './icon.component';

/** Miniatura da imagem principal do item (blob autenticado) com fallback de ícone. */
@Component({
  selector: 'app-thumb',
  imports: [IconeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { style: 'display: contents' },
  template: `
    @if (src(); as s) {
      <img class="thumb" [class]="classe()" [src]="s" [alt]="alt()" loading="lazy" />
    } @else {
      <span class="sem-foto" [class]="classe()"><app-icone nome="caixa" [tamanho]="tamanhoIcone()" /></span>
    }
  `,
})
export class ThumbComponent {
  private readonly imagens = inject(ImagemPrincipalService);
  readonly idItem = input.required<number>();
  readonly possui = input<boolean | undefined>(false);
  readonly versao = input<string | null | undefined>(null);
  readonly alt = input('');
  readonly classe = input('');
  readonly tamanhoIcone = input(18);

  protected readonly src = computed(() =>
    this.possui() ? this.imagens.url(this.idItem(), this.versao())() : null,
  );
}
