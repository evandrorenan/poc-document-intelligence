package com.example.documentintelligence.domain.model;

import lombok.Getter;

import java.util.List;

@Getter
public enum DocumentType {
    RG,
    CPF,
    COMPROVANTE_RESIDENCIA,
    REGISTRO_MATRICULA,
    APOLICE_SEGURO;
}
