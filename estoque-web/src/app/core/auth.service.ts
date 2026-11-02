import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Usuario {
  id: number;
  login: string;
  nome: string;
  perfil: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEm: string;
  usuario: Usuario;
}

const CHAVE_TOKEN = 'estoque.token';
const CHAVE_USUARIO = 'estoque.usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _token = signal<string | null>(localStorage.getItem(CHAVE_TOKEN));
  private readonly _usuario = signal<Usuario | null>(this.lerUsuario());

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();
  readonly autenticado = computed(() => !!this._token());

  login(login: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { login, senha }).pipe(
      tap((r) => {
        localStorage.setItem(CHAVE_TOKEN, r.token);
        this._token.set(r.token);
        this.salvarUsuario(r.usuario);
      }),
    );
  }

  carregarUsuario(): void {
    if (!this._token()) return;
    this.http.get<Usuario>(`${environment.apiUrl}/auth/me`).subscribe({
      next: (u) => this.salvarUsuario(u),
      error: () => {},
    });
  }

  logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
    this._token.set(null);
    this._usuario.set(null);
    this.router.navigate(['/login']);
  }

  private salvarUsuario(u: Usuario | null): void {
    this._usuario.set(u);
    if (u) localStorage.setItem(CHAVE_USUARIO, JSON.stringify(u));
  }

  private lerUsuario(): Usuario | null {
    try {
      const v = localStorage.getItem(CHAVE_USUARIO);
      return v ? (JSON.parse(v) as Usuario) : null;
    } catch {
      return null;
    }
  }
}
