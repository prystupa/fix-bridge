package com.prystupa

import quickfix._
import org.slf4j.LoggerFactory
import akka.actor.{Props, ActorSystem}

object BankApp extends App {

  println("Hello from Bank!")

  val server = new BankApp
  val settings = new SessionSettings(classOf[BankApp].getResourceAsStream("bank.fix.settings"))
  val storeFactory = new FileStoreFactory(settings)
  val messageFactory = new DefaultMessageFactory
  val acceptor = new SocketAcceptor(server, storeFactory, settings, null, messageFactory)
  acceptor.start()
}

class BankApp extends MessageCracker with quickfix.Application {

  private val logger = LoggerFactory.getLogger(classOf[BankApp])
  private val akka = ActorSystem.create("bank-app")
  private val rfq = akka.actorOf(Props(new RfqGenerator))

  def onCreate(session: SessionID) {
    logger.info("Session created: {}", session)
  }

  def onLogon(session: SessionID) {
    logger.info("Logon for session: {}", session)
  }

  def onLogout(session: SessionID) {
    logger.info("Logout for session: {}", session)
  }

  def toAdmin(message: Message, session: SessionID) {
    logger.trace("toAdmin ({}, {})", List(message, session): _*)
  }

  def fromAdmin(message: Message, session: SessionID) {
    logger.trace("fromAdmin ({}, {})", List(message, session): _*)
  }

  def toApp(message: Message, session: SessionID) {
    logger.trace("toApp ({}, {})", List(message, session): _*)
  }

  def fromApp(message: Message, session: SessionID) {
    logger.trace("fromApp ({}, {})", List(message, session): _*)

    crack(message, session)
  }

  def onMessage(message: quickfix.fix42.QuoteRequest, sessionID: SessionID) {
    logger.info("Received RFQ request: ({}, {})", List(message, sessionID): _*)

    rfq ! RfqGenerator.Request(message, sessionID)
  }

  def onMessage(message: quickfix.fix42.QuoteCancel, sessionID: SessionID) {
    logger.info("Received RFQ Cancel request: ({}, {})", List(message, sessionID): _*)

    rfq ! RfqGenerator.Cancel(message, sessionID)
  }
}
