package br.pucpr.authserver.mensagens


import br.pucpr.authserver.mensagens.Mensagens
import br.pucpr.authserver.mensagens.MensagensRepository
import org.springframework.stereotype.Service

@Service
class ServiceMensagem(
    val repository: MensagensRepository
) {

    // Função para listar todas as mensagens
    fun lista(): List<Mensagens> {
        return repository.findAll()
    }

    // Função para inserir uma nova mensagem
    fun insert(mensagem: Mensagens): Mensagens {
        return repository.save(mensagem)
    }

    fun listaEntreUsuarios(remetente: String, destinatario: String): List<Mensagens> {
        return repository.findByEmailUserAndEmailDestinatario(remetente, destinatario)
    }
}

