package com.davylucas.estoque.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tab_marca", schema = "apl_estoque")
public class Marca extends CadastroAuxiliar {
}