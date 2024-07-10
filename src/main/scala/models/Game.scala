package models

class Game(
            val deck: Deck,
            val communityCards: List[Card],
            val player: Player,
            val dealer: Dealer,
            val bet: Int,
            val pot: Int,
            val inGame: Boolean
          ) {
  def getDeck: Deck = deck
  def getCommunityCards: List[Card] = communityCards
  def getPlayer: Player = player
  def getDealer: Dealer = dealer
  def getBet: Int = bet
  def getPot: Int = pot

  def resetGame(): Game = {
    val newDeck = new Deck().init()
    val newCommunityCards = List()
    val newPlayer = new Player("Player", List(), 100)
    val newDealer = new Dealer("Dealer", List())
    val newBet = 0
    val newPot = 0
    new Game(newDeck, newCommunityCards, newPlayer, newDealer, newBet, newPot, true)
  }

  def startGame(bet: Int): Game = {
    val newDeck = new Deck().init()
    player.bet(bet)
    val newPot = pot + bet
    val newGame = new Game(newDeck, communityCards, player, dealer, bet, newPot, true)
    newGame.dealPersonalCards()
    newGame
  }

  def askForBet(): Int = {
    val minBet = 10
    val maxBet = 100
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
        case e: IllegalArgumentException => println(e.getMessage)
        case _: NumberFormatException => println("Invalid input. Please enter a numeric value.")
      }
    }
    player.bet(betAmount)
    betAmount
  }

  def dealPersonalCards(): Unit = {
    player.receiveCards(deck.deal(2))
    dealer.receiveCards(deck.deal(2))
  }

  def dealCommunityCards(): Game = {
    val newCards = deck.deal(3)
    new Game(deck, newCards, player, dealer, bet, pot, true)
  }

  def revealDealerHand(): Game = {
    val dealerHand = dealer.getHand
    new Game(deck, communityCards, player, dealer, bet, pot, true)
  }

  def evaluateHands(): Unit = {
    val playerHand = new Hand(player.getHand ++ communityCards)
    val dealerHand = new Hand(dealer.getHand ++ communityCards)

    playerHand.cards.sortBy(_.getValue).reverse
    playerHand.cards.sortBy(_.getValue).reverse

    println(s"Player's hand: ${playerHand}")
    println(s"Dealer's hand: ${dealerHand}")

    val result = playerHand.compareHands(dealerHand)
    if (result > 0) {
      println("Player wins!")
    } else if (result < 0) {
      println("Dealer wins!")
    } else {
      println("It's a tie!")
    }
  }

  def continueGame(): Unit = {
    println("Would you like to play another round? (yes/no)")
    val answer = scala.io.StdIn.readLine().trim.toLowerCase
    if (answer == "yes") {
      val newGame = Game.createNewGame(player, dealer)
      newGame.gameLoop()
    } else {
      println("Thanks for playing!")
    }
  }

  def gameLoop(): Unit = {
    println("Starting a new game...")
    try {
      val betAmount = askForBet()  // Ask player for a bet
      println(s"Player placed a bet of $betAmount")

      val gameWithInitialBet = startGame(betAmount)  // Start the game with the player's bet
      println(s"Player's hand: ${gameWithInitialBet.getPlayer.getHand.mkString(", ")}")
      println(s"Dealer's hand: ${gameWithInitialBet.getDealer.getHand.mkString(", ")}")

      val gameWithCommunityCards = gameWithInitialBet.dealCommunityCards()  // Deal community cards
      println(s"Community Cards: ${gameWithCommunityCards.getCommunityCards.mkString(", ")}")

      gameWithCommunityCards.evaluateHands()  // Evaluate hands

      gameWithCommunityCards.continueGame()  // Ask if the player wants to play another round
    } catch {
      case e: Exception => println(s"Error: ${e.getMessage}")
    }
  }

  override def toString: String = s"${player.name} starts game betting ${this.bet}."
}

object Game {
  def createNewGame(player: Player, dealer: Dealer): Game = {
    val newDeck = new Deck().init()
    player.startNewGame()
    dealer.startNewGame()
    new Game(newDeck, List(), player, dealer, 0, 0, false)
  }
}
