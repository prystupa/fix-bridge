package com.prystupa

import akka.actor.Actor
import quickfix.{SessionID, Session}
import quickfix.fix42.QuoteRequest
import quickfix.field.{QuoteCancelType, QuoteID, QuoteReqID}
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 3/31/13
 * Time: 12:12 PM
 */

object VenueClient {

  case class ProviderSession(session: SessionID)

  case class Subscribe(symbol: String, qty: Double)

  case class Cancel(symbol: String)

  case class Quote(message: quickfix.fix42.Quote, session: SessionID)

}

class VenueClient() extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[VenueClient])
  var provider: SessionID = _


  def receive = {
    case VenueClient.ProviderSession(session) => provider = session
    case VenueClient.Subscribe(symbol, qty) => sendRfqRequest(symbol, qty)
    case VenueClient.Cancel(symbol) => sendCancelRfqRequest(symbol)
    case VenueClient.Quote(message, sessionID) => logger.info("Received quote: ({}, {})", List(message, sessionID): _*)

  }

  private def sendRfqRequest(symbol: String, qty: Double) {
    logger.info("Sending RFQ request for ({}, {})", symbol, qty)

    val session = Session.lookupSession(provider)

    val group = new QuoteRequest.NoRelatedSym()
    group.set(new quickfix.field.Symbol(symbol))

    val message = new quickfix.fix42.QuoteRequest()
    message.set(new QuoteReqID(UUID.randomUUID().toString))
    message.addGroup(group)
    session.send(message)
  }

  private def sendCancelRfqRequest(symbol: String) {
    logger.info("Cancelling RFQ request for ({})", symbol)

    val session = Session.lookupSession(provider)

    val group = new quickfix.fix42.QuoteCancel.NoQuoteEntries()
    group.set(new quickfix.field.Symbol(symbol))

    val message = new quickfix.fix42.QuoteCancel(new QuoteID("all for symbol"), new QuoteCancelType(QuoteCancelType.CANCEL_FOR_SYMBOL))
    message.addGroup(group)
    session.send(message)
  }
}
