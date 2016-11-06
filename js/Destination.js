import React, { Component } from 'react';

class Destination extends Component {
  render() {
    return (
      <div className="destination">
        <span>Country: {this.props.country} | City: {this.props.city} | Count: {this.props.count}</span>
      </div>
    );
  }
}

export default Destination;