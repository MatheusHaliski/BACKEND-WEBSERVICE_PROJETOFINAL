package br.pucpr.authserver.mensagens
import br.pucpr.authserver.mensagens.MensagensG
import br.pucpr.authserver.mensagens.MensagensGRepository
import org.springframework.stereotype.Service

@Service
class ServiceMensagemG (
    val repositoryg: MensagensGRepository
){

    // Função para listar todas as mensagens
    fun lista(): List<MensagensG> {
        return repositoryg.findAll()
    }
    // Função para inserir uma nova mensagem
    fun insert(mensagem: MensagensG): MensagensG {
        return repositoryg.save(mensagem)
    }
     }