package com.davylucas.estoque.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tab_cor", schema = "apl_estoque")
public class Cor extends CadastroAuxiliar {
}