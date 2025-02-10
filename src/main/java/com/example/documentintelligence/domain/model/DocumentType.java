package com.example.documentintelligence.domain.model;

import lombok.Getter;

import java.util.List;

@Getter
public enum DocumentType {
    RG(List.of(
            "documentoIdentificacao.tipoDocumento.nome",
            "documentoIdentificacao.tipoDocumento.nomeSocial",
            "documentoIdentificacao.tipoDocumento.filiacao.pai",
            "documentoIdentificacao.tipoDocumento.filiacao.mae",
            "documentoIdentificacao.tipoDocumento.dataNascimento",
            "documentoIdentificacao.tipoDocumento.naturalidade",
            "documentoIdentificacao.tipoDocumento.numeroDocumento",
            "documentoIdentificacao.tipoDocumento.dataExpedicao")),
    CPF(List.of("NOME", "NUMERO_CPF")),
    COMPROVANTE_RESIDENCIA(List.of("NOME", "ENDERECO")),
    REGISTRO_MATRICULA(List.of(
            "propriedadeUrbana.proprietarios[*].nome",
            "propriedadeUrbana.area",
            "propriedadeUrbana.logradouro",
            "propriedadeUrbana.bairro",
            "propriedadeUrbana.cidade",
            "propriedadeUrbana.uf",
            "propriedadeUrbana.dataMatricula",
            "propriedadeUrbana.numeroMatricula",
            "propriedadeUrbana.valorVenda")),
    APOLICE_SEGURO ( List.of("NOME"));

    private final List<String> fields;

    DocumentType(List<String> fields) {
        this.fields = fields;
    }
}
