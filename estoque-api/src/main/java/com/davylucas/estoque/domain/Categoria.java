package com.davylucas.estoque.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tab_categoria", schema = "apl_estoque")
public class Categoria extends CadastroAuxiliar {
}