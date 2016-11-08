package protocols

sealed trait DestinationFeedAggregatorProtocol

case object DestinationFeedAggregatorSubscribe extends DestinationFeedAggregatorProtocol
case object DestinationFeedAggregatorUnSubscribe extends DestinationFeedAggregatorProtocol