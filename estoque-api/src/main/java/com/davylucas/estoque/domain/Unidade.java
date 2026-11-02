package com.davylucas.estoque.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tab_unidade", schema = "apl_estoque")
public class Unidade extends CadastroAuxiliar {
}