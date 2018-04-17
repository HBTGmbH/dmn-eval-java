(function nashornEventLoopMain(context) {
  'use strict';

  var ArrayDeque = Java.type('java.util.ArrayDeque');
  var Phaser = Java.type('java.util.concurrent.Phaser');

  var eventLoop;
  var phaser = new Phaser();

  resetEventLoop();

  function resetEventLoop() {
    eventLoop = new ArrayDeque();
  }

  function waitForMessages() {
    phaser.register();
    var wait = !(eventLoop.size() === 0);
    phaser.arriveAndDeregister();

    return wait;
  }

  function processNextMessages() {
    var remaining = 1;
    while (remaining) {
      phaser.register();
      var message = eventLoop.removeFirst();
      remaining = eventLoop.size();
      phaser.arriveAndDeregister();

      var fn = message.fn;
      var args = message.args;

      try {
        fn.apply(context, args);
      } catch (e) {
        console.trace(e);
        console.trace(fn);
        console.trace(args);
      }
    }
  }

  context.nashornEventLoop = {
    process: function () {
      while (waitForMessages()) {
        processNextMessages()
      }
    },
    reset: resetEventLoop
  };


  function addToEventLoop(fn, args) {
    eventLoop.addLast({
        fn: fn,
        args: args
    });
  }

  var setTimeout = function (fn, millis /* [, args...] */) {
    var args = [].slice.call(arguments, 2, arguments.length);

    addToEventLoop(fn, args);
    return undefined;
  };

  var setImmediate = function (fn /* [, args...] */) {
    var args = [].slice.call(arguments, 1, arguments.length);
    return setTimeout(fn, 0, args);
  }

  context.setTimeout = setTimeout;
  context.setImmediate = setImmediate;
})(typeof global !== "undefined" && global || typeof self !== "undefined" && self || this);
