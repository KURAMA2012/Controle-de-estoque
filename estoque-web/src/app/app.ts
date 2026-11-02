import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/theme.service';
import { ConfirmacaoComponent } from './shared/confirmacao.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ConfirmacaoComponent],
  template: '<router-outlet /><app-confirmacao />',
})
export class App {
  // instancia cedo para aplicar o tema salvo
  private readonly tema = inject(ThemeService);
}
