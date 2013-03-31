package com.prystupa

import quickfix._
import org.slf4j.LoggerFactory

object BridgeApp extends App {

  println("Hello from Bridge!")

  val bridge = new BridgeApp
  val settings = new SessionSettings(classOf[BridgeApp].getResourceAsStream("bridge.fix.settings"))
  val storeFactory = new FileStoreFactory(settings)
  val messageFactory = new DefaultMessageFactory
  val initiator = new SocketInitiator(bridge, storeFactory, settings, null, messageFactory)

  initiator.start()

  println("[Enter] to quit")
  readLine()
}

class BridgeApp extends MessageCracker with quickfix.Application {

  private val logger = LoggerFactory.getLogger(classOf[BridgeApp])

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

    val targetSession = Session.lookupSession(new SessionID("FIX.4.2", "BRIDGE", "BANK"))
    targetSession.send(message)
  }

  def onMessage(message: quickfix.fix42.QuoteCancel, sessionID: SessionID) {
    logger.info("Received RFQ Cancel request: ({}, {})", List(message, sessionID): _*)

    val targetSession = Session.lookupSession(new SessionID("FIX.4.2", "BRIDGE", "BANK"))
    targetSession.send(message)
  }

  def onMessage(message: quickfix.fix42.Quote, sessionID: SessionID) {
    logger.info("Received quote: ({}, {})", List(message, sessionID): _*)

    val targetSession = Session.lookupSession(new SessionID("FIX.4.2", "BRIDGE", "VENUE"))
    targetSession.send(message)
  }
}