package br.pucpr.authserver.mensagens
import br.pucpr.authserver.mensagens.Mensagens
import br.pucpr.authserver.mensagens.MensagensRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mensagens")
class MensagemController(
val service : ServiceMensagem,
){
    @PostMapping("/criarmt")
    fun criarMensagem(@RequestBody mensagemDto: MensagemDTO): ResponseEntity<Mensagens> {
        // Construa a nova mensagem usando os dados recebidos
        val mensagem = Mensagens(
            emailUser = mensagemDto.emailUser,
            emailDestinatario = mensagemDto.emailDestinatario,
            messageContent = mensagemDto.messageContent,
        )
        val novaMensagem = service.insert(mensagem)
        return ResponseEntity.ok(novaMensagem)
    }

    @GetMapping("/listarmt/{remetente}/{destinatario}")
    fun listarMensagens(@PathVariable remetente: String, @PathVariable destinatario: String): List<Mensagens> {
        // Obtém as mensagens de ambos os usuários
        val mensagensRemetenteDestinatario = service.listaEntreUsuarios(remetente, destinatario)
        val mensagensDestinatarioRemetente = service.listaEntreUsuarios(destinatario, remetente)

        // Combina as listas
        val todasMensagens = mensagensRemetenteDestinatario + mensagensDestinatarioRemetente

        // Ordena as mensagens por ID
        return todasMensagens.sortedBy { it.id }
    }

}
