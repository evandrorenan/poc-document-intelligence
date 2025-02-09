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
            "NOME_PROPRIETARIO",
            "AREA_IMOVEL",
            "ENDERECO_IMOVEL",
            "BAIRRO",
            "CIDADE",
            "ESTADO",
            "AREA_IMOVEL",
            "DATA_MATRICULA",
            "VALOR_COMPRA")),
    APOLICE_SEGURO ( List.of("NOME"));

    private final List<String> fields;

    DocumentType(List<String> fields) {
        this.fields = fields;
    }
}
