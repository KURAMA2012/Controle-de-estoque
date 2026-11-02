import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CadastroAuxiliar {
  id?: number;
  descricao: string;
  ativo: boolean;
}

export interface Marca extends CadastroAuxiliar {}

export interface Modelo extends CadastroAuxiliar {
  marca: { id: number; descricao?: string } | null;
}

/** Cliente genérico dos cadastros auxiliares: /{endpoint}/listar, buscar, inserir, {id}/atualizar, deletar. */
@Injectable({ providedIn: 'root' })
export class CadastroService {
  private readonly http = inject(HttpClient);

  private url(endpoint: string): string {
    return `${environment.apiUrl}/${endpoint}`;
  }

  listar<T extends CadastroAuxiliar>(endpoint: string, ativos?: boolean): Observable<T[]> {
    let params = new HttpParams();
    if (ativos !== undefined) params = params.set('ativos', String(ativos));
    return this.http.get<T[]>(`${this.url(endpoint)}/listar`, { params });
  }

  buscar<T extends CadastroAuxiliar>(endpoint: string, id: number): Observable<T> {
    return this.http.get<T>(`${this.url(endpoint)}/buscar/${id}`);
  }

  inserir<T extends CadastroAuxiliar>(endpoint: string, entidade: T): Observable<T> {
    return this.http.post<T>(`${this.url(endpoint)}/inserir`, entidade);
  }

  atualizar<T extends CadastroAuxiliar>(endpoint: string, id: number, entidade: T): Observable<T> {
    return this.http.put<T>(`${this.url(endpoint)}/${id}/atualizar`, entidade);
  }

  deletar(endpoint: string, id: number): Observable<boolean> {
    return this.http.delete<boolean>(`${this.url(endpoint)}/deletar/${id}`);
  }

  listarModelosPorMarca(idMarca: number): Observable<Modelo[]> {
    return this.http.get<Modelo[]>(`${this.url('modelo')}/listar-por-marca/${idMarca}`);
  }
}
