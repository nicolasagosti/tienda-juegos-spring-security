package com.example.tiendajuegos.model;

/**
 * Las 3 categorias (roles) de usuario que pide el enunciado:
 *
 *  - ADMIN:      administra usuarios, secciones y modera todo el contenido.
 *  - VENDEDOR:   puede publicar y editar SUS PROPIOS juegos (precio + imagen).
 *  - COMPRADOR:  solo puede ver el catalogo de juegos.
 *
 * Se guarda en la base de datos como texto (EnumType.STRING) para que sea
 * legible y no dependa del orden de declaracion.
 */
public enum Rol {
    ADMIN,
    VENDEDOR,
    COMPRADOR
}
