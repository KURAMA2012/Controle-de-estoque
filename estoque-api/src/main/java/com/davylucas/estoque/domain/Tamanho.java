package com.davylucas.estoque.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tab_tamanho", schema = "apl_estoque")
public class Tamanho extends CadastroAuxiliar {
}