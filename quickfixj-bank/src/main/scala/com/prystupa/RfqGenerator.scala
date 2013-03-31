package com.prystupa

import akka.actor.{Cancellable, Actor}
import quickfix.fix42.{QuoteCancel, Quote, QuoteRequest}
import quickfix.{Session, SessionID}
import scala.concurrent.duration._
import com.prystupa.RfqGenerator.Request
import quickfix.field.QuoteID
import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 3/31/13
 * Time: 9:50 AM
 */

object RfqGenerator {

  case class Request(message: QuoteRequest, sessionID: SessionID)

  case class Cancel(message: QuoteCancel, sessionID: SessionID)

}

class RfqGenerator extends Actor {

  private var requests: List[RfqGenerator.Request] = Nil
  private var ticks: Cancellable = _

  override def preStart() {
    val system = context.system
    import system.dispatcher
    ticks = system.scheduler.schedule(0 milliseconds, 5000 milliseconds, self, 'tick)
  }

  override def postStop() {
    ticks.cancel()
  }

  def receive = {
    case request: RfqGenerator.Request => requests = request :: requests
    case cancel: RfqGenerator.Cancel => requests = requests filterNot (r => isMatch(r, cancel))
    case 'tick => requests.foreach(generateQuote)
  }

  private def generateQuote(request: Request) {
    val session = Session.lookupSession(request.sessionID)
    val quoteRequest = request.message
    val symbol = getSymbol(quoteRequest)
    val quote = new Quote(new QuoteID(UUID.randomUUID().toString), symbol)
    session.send(quote)
  }


  private def getSymbol(message: QuoteRequest): quickfix.field.Symbol = {
    val symbol = message.getGroup(1, new QuoteRequest.NoRelatedSym()).asInstanceOf[QuoteRequest.NoRelatedSym].getSymbol
    symbol
  }

  private def getSymbol(message: QuoteCancel): quickfix.field.Symbol = {
    val symbol = message.getGroup(1, new QuoteCancel.NoQuoteEntries()).asInstanceOf[QuoteCancel.NoQuoteEntries].getSymbol
    symbol
  }

  private def isMatch(request: Request, cancel: RfqGenerator.Cancel): Boolean = {
    request.sessionID == cancel.sessionID &&
      getSymbol(request.message) == getSymbol(cancel.message)
  }
}
