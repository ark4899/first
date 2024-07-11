package models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HandSpec extends AnyFlatSpec with Matchers {

  // Check if evaluateHand returns the correct HandScore

  "Hand" should "evaluate to a flush" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Hearts", "4"),
      new Card("Hearts", "6"),
      new Card("Hearts", "8"),
      new Card("Hearts", "10")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.Flush
  }

  it should "evaluate to a straight" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "4"),
      new Card("Clubs", "3"),
      new Card("Spades", "6"),
      new Card("Hearts", "5")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.Straight
  }

  it should "evaluate to a low ace straight" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "3"),
      new Card("Clubs", "4"),
      new Card("Spades", "Ace"),
      new Card("Hearts", "5")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.Straight
  }

  it should "evaluate to three of a kind" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "2"),
      new Card("Clubs", "2"),
      new Card("Spades", "5"),
      new Card("Hearts", "6")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.ThreeOfAKind
  }

  it should "evaluate to two pairs" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "2"),
      new Card("Clubs", "3"),
      new Card("Spades", "3"),
      new Card("Hearts", "6")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.TwoPairs
  }

  it should "evaluate to one pair" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "2"),
      new Card("Clubs", "4"),
      new Card("Spades", "5"),
      new Card("Hearts", "6")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.OnePair
  }

  it should "evaluate to a high card" in {
    val cards = List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "4"),
      new Card("Clubs", "6"),
      new Card("Spades", "8"),
      new Card("Hearts", "10")
    )
    val hand = new Hand(cards)
    hand.evaluateHand shouldEqual HandScore.HighCard
  }

  // Check if compareHands returns the correct comparison result

  it should "compare hands correctly" in {
    val flushHand = new Hand(List(
      new Card("Hearts", "2"),
      new Card("Hearts", "4"),
      new Card("Hearts", "6"),
      new Card("Hearts", "8"),
      new Card("Hearts", "10")
    ))
    val straightHand = new Hand(List(
      new Card("Diamonds", "2"),
      new Card("Diamonds", "3"),
      new Card("Diamonds", "4"),
      new Card("Diamonds", "5"),
      new Card("Diamonds", "6")
    ))
    flushHand.compareHands(straightHand) shouldEqual 1 // Flush beats straight

    val threeOfAKindHand = new Hand(List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "2"),
      new Card("Clubs", "2"),
      new Card("Spades", "5"),
      new Card("Hearts", "6")
    ))
    straightHand.compareHands(threeOfAKindHand) shouldEqual 1 // Straight beats three of a kind

    val twoPairsHand = new Hand(List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "2"),
      new Card("Clubs", "3"),
      new Card("Spades", "3"),
      new Card("Hearts", "6")
    ))
    threeOfAKindHand.compareHands(twoPairsHand) shouldEqual 1 // Three of a kind beats two pairs

    val onePairHand = new Hand(List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "2"),
      new Card("Clubs", "4"),
      new Card("Spades", "5"),
      new Card("Hearts", "6")
    ))
    twoPairsHand.compareHands(onePairHand) shouldEqual 1 // Two pairs beats one pair

    val highCardHand = new Hand(List(
      new Card("Hearts", "2"),
      new Card("Diamonds", "4"),
      new Card("Clubs", "6"),
      new Card("Spades", "8"),
      new Card("Hearts", "10")
    ))
    onePairHand.compareHands(highCardHand) shouldEqual 1 // One pair beats high card
  }

  // Check if compareFlush returns the correct comparison result

  it should "compare flushes by suit" in {
    val hand1Cards = List(
      new Card("Hearts", "2"),
      new Card("Hearts", "4"),
      new Card("Hearts", "6"),
      new Card("Hearts", "8"),
      new Card("Hearts", "10")
    )
    val hand2Cards = List(
      new Card("Diamonds", "2"),
      new Card("Diamonds", "4"),
      new Card("Diamonds", "6"),
      new Card("Diamonds", "8"),
      new Card("Diamonds", "10")
    )
    val hand1 = new Hand(hand1Cards)
    val hand2 = new Hand(hand2Cards)

    hand1.compareHands(hand2) shouldEqual 1 // Hearts flush beats Diamonds flush
  }
}
