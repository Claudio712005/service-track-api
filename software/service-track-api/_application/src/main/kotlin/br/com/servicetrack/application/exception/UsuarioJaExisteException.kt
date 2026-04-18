package br.com.servicetrack.application.exception

class UsuarioJaExisteException(email: String, cpf: String) :
    RuntimeException("Usuário com email '$email' e/ou CPF '$cpf' já cadastrado")
