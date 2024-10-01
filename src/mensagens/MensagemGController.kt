package br.pucpr.authserver.mensagens
import br.pucpr.authserver.mensagens.MensagensG
import br.pucpr.authserver.mensagens.MensagensGRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mensagensG")
class MensagemGController(
    val serviceg : ServiceMensagemG
) {
    @PostMapping("/criarm")
    fun criarMensagem(@RequestBody mensagem: MensagensG): ResponseEntity<MensagensG> {
        val novaMensagem = serviceg.insert(mensagem)
        return ResponseEntity.ok(novaMensagem)
    }
    @GetMapping("/listarm")
    fun listarMensagens(): List<MensagensG> {
        return serviceg.lista()
    }
}