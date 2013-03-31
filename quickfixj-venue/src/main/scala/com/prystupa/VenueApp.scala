package com.prystupa

import quickfix._
import org.slf4j.LoggerFactory
import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import java.util.concurrent.CountDownLatch

object VenueApp extends App {

  println("Hello from Venue!")

  val server = new VenueApp
  val settings = new SessionSettings(classOf[VenueApp].getResourceAsStream("venue.fix.settings"))
  val storeFactory = new FileStoreFactory(settings)
  val messageFactory = new DefaultMessageFactory
  val acceptor = new SocketAcceptor(server, storeFactory, settings, null, messageFactory)
  acceptor.start()

  server.awaitProviderLogon()
  server.simulateTradingSession()
}

class VenueApp extends MessageCracker with quickfix.Application {

  private val logger = LoggerFactory.getLogger(classOf[VenueApp])
  private val akka = ActorSystem.create("venue-app")
  private val client = akka.actorOf(Props(new VenueClient))
  private val logon = new CountDownLatch(1)

  def awaitProviderLogon() {

    logger.info("Awaiting logon from provider")
    logon.await()
  }

  def simulateTradingSession() {
    import akka.dispatcher

    akka.scheduler.scheduleOnce(1 seconds, client, VenueClient.Subscribe("USD/CAD", 1e6))
    akka.scheduler.scheduleOnce(10 seconds, client, VenueClient.Subscribe("EUR/USD", 2e6))
    akka.scheduler.scheduleOnce(15 seconds, client, VenueClient.Cancel("USD/CAD"))
    akka.scheduler.scheduleOnce(30 seconds, client, VenueClient.Cancel("EUR/USD"))
    akka.scheduler.scheduleOnce(40 seconds, new Runnable {
      def run() {
        simulateTradingSession()
      }
    })
  }

  def onCreate(session: SessionID) {
    logger.info("Session created: {}", session)
    client ! VenueClient.ProviderSession(session)
  }

  def onLogon(session: SessionID) {
    logger.info("Logon for session: {}", session)
    logon.countDown()
  }

  def onLogout(session: SessionID) {
    logger.info("Logout for session: {}", session)
  }

  def toAdmin(message: Message, session: SessionID) {
    logger.trace("toAdmin ({}, {})", List(message, session): _*)
  }

  def fromAdmin(message: Message, session: SessionID) {
    logger.info("fromAdmin ({}, {})", List(message, session): _*)
  }

  def toApp(message: Message, session: SessionID) {
    logger.trace("toApp ({}, {})", List(message, session): _*)
  }

  def fromApp(message: Message, session: SessionID) {
    logger.trace("fromApp ({}, {})", List(message, session): _*)

    crack(message, session)
  }

  def onMessage(message: quickfix.fix42.Quote, sessionID: SessionID) {
    client ! VenueClient.Quote(message, sessionID)
  }

}