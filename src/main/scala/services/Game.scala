package services

import models.HandScore.HandScore
import models._
import repositories.GameRepositorySingleton
import repositories.documents.{DealerDocument, GameDocument, PlayerDocument}
import services.Game.createNewGame

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class Game(
            val deck: Deck,
            val communityCards: List[Card],
            val player: Player,
            val dealer: Dealer,
            val bet: Int,
            val pot: Int
          ) {

  private val minBet = 5
  private val maxBet = 100

  private def getCommunityCards: List[Card] = communityCards

  var winnerName: String = ""
  var higherScore: Option[HandScore] = None

  def startGame(bet: Int): Game = {
    val newPot = pot + bet
    val newGame = new Game(deck, communityCards, player, dealer, bet, newPot)
    newGame
  }

  private def askForBet(): Option[Int] = {
    println("\nDo you want to place a bet or fold? (f to fold, any other key to bet)")
    val action = scala.io.StdIn.readLine().trim.toLowerCase

    if (action == "f") {
      None
    } else {
      var validBet = false
      var betAmount = 0

      while (!validBet) {

        try {
          println(s"Please enter your bet amount (min $minBet, max $maxBet):")
          betAmount = scala.io.StdIn.readInt()

          if (betAmount < minBet) {
            throw new IllegalArgumentException(s"Bet amount must be at least $minBet")
          } else if (betAmount > maxBet) {
            throw new IllegalArgumentException(s"Bet amount must be at most $maxBet")
          } else if (betAmount > player.getChips) {
            throw new IllegalArgumentException("Insufficient chips for the bet amount")
          } else {
            validBet = true
          }

        } catch {
          case _: NumberFormatException => println("Invalid input. Please enter a numeric value.")
          case e: IllegalArgumentException => println(e.getMessage)
        }
      }
      player.bet(betAmount)
      Some(betAmount)
    }
  }

  def dealPersonalCards(): Unit = {
    player.receiveCards(deck.deal(2))
    dealer.receiveCards(deck.deal(2))
  }

  def dealCommunityCards(): Game = {
    val newCards = communityCards ++ deck.deal(3)
    new Game(deck, newCards, player, dealer, bet, pot)
  }

  def revealDealerHand(): Unit = {
    println(s"Revealing dealer's cards: ${dealer.getHand.mkString(", ")}")
  }

  def evaluateHands(): Unit = {
    val playerHand = new Hand(player.getHand ++ communityCards)
    val dealerHand = new Hand(dealer.getHand ++ communityCards)

    playerHand.cards.sortBy(_.getValue).reverse
    playerHand.cards.sortBy(_.getValue).reverse

    println(s"${player.getName}'s cards: ${playerHand.cards.mkString(", ")}")
    println(s"Dealer's cards: ${dealerHand.cards.mkString(", ")}")

    println(s"\n${player.getName}'s hand: ${playerHand.getHandScore}")
    println(s"Dealer's hand: ${dealerHand.getHandScore}")

    printSeparator()

    Thread.sleep(1000)

    val result = playerHand.compareHands(dealerHand)

    if (result > 0) {
      println(s"${player.getName} wins ${pot} chips with ${playerHand.getHandScore}!")
      player.winHand(bet *2)
      winnerName = player.getName
      higherScore = Some(playerHand.getHandScore)

    } else if (result < 0) {
      println(s"Dealer wins with ${dealerHand.getHandScore}, ${player.getName} loses ${pot} chips.")
      winnerName = dealer.getName
      higherScore = Some(dealerHand.getHandScore)

    } else {
      println("It's a tie!")
      player.addChips(bet)
      winnerName = "Tie"
      higherScore = Some(playerHand.getHandScore)
    }

    printSeparator()
  }

  def continueGame(): Unit = {
    if (player.getChips <= minBet) {
      println(s"${player.getName} has not enough chips for playing another game. Game over!")
      return
    }
    println("\nWould you like to play another round? (y/n)")
    val answer = scala.io.StdIn.readLine().trim.toLowerCase
    if (answer == "y") {
      val newGame = createNewGame(player, dealer)
      newGame.gameLoop()
    } else {
      println(s"\n${player.getName} leaves the game with ${player.getChips} chips and ${player.getTotalWins} wins.")
      println("\nThanks for playing!\n")
    }
  }

  def gameLoop(): Unit = {
    println(s"Welcome ${player.name}! Starting a new game...")
    try {

      println(s"\n${player.getName}'s chips: ${player.getChips}")

      dealPersonalCards()  // Deal cards to player and dealer
      println(s"${player.getName}'s hand: ${player.getHand.mkString(", ")}")

      askForBet() match {
        case Some(betAmount) =>
          println(s"${player.getName} placed a bet of $betAmount")
          printSeparator()

          val gameWithInitialBet = startGame(betAmount)  // Start the game with the player's bet
          val gameWithCommunityCards = gameWithInitialBet.dealCommunityCards()  // Deal community cards
          println(s"Dealing community cards: ${gameWithCommunityCards.getCommunityCards.mkString(", ")}")

          gameWithCommunityCards.revealDealerHand()  // Reveal dealer's cards

          printSeparator()

          gameWithCommunityCards.evaluateHands()  // Evaluate hands

          try {
            // Save the game to the database
            gameWithCommunityCards.saveGame()
          } catch {
            case e: Exception => println(s"Error saving game: ${e.getMessage}")
          }

          Thread.sleep(1000)

          gameWithCommunityCards.continueGame()  // Ask if the player wants to play another round

        case None =>
          println(s"${player.getName} folded!")
          continueGame()
      }
    } catch {
      case e: Exception => println(s"Error: ${e.getMessage}")
    }
  }

  // Save the current game state to the database
  def saveGame(): Unit = {

    val gameDoc = GameDocument(
      _id = None,
      gameDateTime = LocalDateTime.now(),
      players = List(
        PlayerDocument(
          player.getName,
          player.getChips,
          player.getHand.map(_.toString),
          new Hand(player.getHand).getHandScore.toString
        )
      ),
      dealer = DealerDocument(
        dealer.getName,
        dealer.getHand.map(_.toString),
        new Hand(dealer.getHand).getHandScore.toString
      ),
      communityCards = communityCards.map(_.toString),
      winner = winnerName,
      higherScore = higherScore.map(_.toString).getOrElse(""),
      pot = pot
    )

    GameRepositorySingleton.getRepository.saveGame(gameDoc).onComplete {
      case scala.util.Success(_) => println("Game saved successfully.")
      case scala.util.Failure(exception) => println(s"Failed to save game: ${exception.getMessage}")
    }
  }

  private def printSeparator(): Unit = {
    println("----------------------------------------------------------------------------------")
  }

  override def toString: String = s"${player.getName} starts game betting ${this.bet}."
}

object Game {
  def createNewGame(player: Player, dealer: Dealer): Game = {
    val newDeck = new Deck().init()
    player.startNewGame()
    dealer.startNewGame()
    new Game(newDeck, List(), player, dealer, 0, 0)
  }

}
