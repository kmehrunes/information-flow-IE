package interfaces

import java.util.concurrent.CompletableFuture

import io.javalin.Javalin

object Server {
  def start(port: Int = 3200): Unit = {
    val app = Javalin.create()
      .disableStartupBanner()
      .start(3210)

    app.post("/ie", ctx => {
      val text = ctx.body()
      val json = CompletableFuture.supplyAsync(() => {
        val result = ie.InformationExtraction.runPipeline(text)
        Formatters.formatJson(result, prettyPrint = false)
      })

      ctx.result(json).contentType("application/json")
    })
  }
}
