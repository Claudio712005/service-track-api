package br.com.servicetrack.application.exception

class UsuarioJaExisteException(email: String, cpf: String) :
    RuntimeException("Usuário com email '$email' e CPF '$cpf' já cadastrado")
