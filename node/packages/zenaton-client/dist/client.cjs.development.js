'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var pulsarClient = require('pulsar-client');
var messages = require('@zenaton/messages');
var uuid = require('uuid');

class Client {
  constructor(opts = {}) {
    this.opts = Object.assign({
      pulsar: {
        client: {}
      }
    }, opts);
  }

  dispatchTask(name, input) {
    try {
      const _this = this;

      const jobId = uuid.v4();
      const message = {
        jobId,
        type: 'DispatchJob',
        DispatchJob: {
          jobId,
          sentAt: Date.now(),
          jobName: name,
          jobData: Buffer.from(JSON.stringify(input)),
          workflowId: null
        }
      };

      _this.dispatchForEngineMessage(message);

      return Promise.resolve();
    } catch (e) {
      return Promise.reject(e);
    }
  }

  dispatchForEngineMessage(message) {
    try {
      const _this2 = this;

      function _temp2() {
        return Promise.resolve(_this2.pulsarProducer.send({
          data: messages.AvroForEngineMessage.toBuffer(message)
        })).then(function () {});
      }

      if (!_this2.pulsarClient) {
        _this2.pulsarClient = new pulsarClient.Client(_this2.opts.pulsar.client);
      }

      const _temp = function () {
        if (!_this2.pulsarProducer) {
          return Promise.resolve(_this2.pulsarClient.createProducer({
            topic: 'persistent://public/default/tasks-engine',
            sendTimeoutMs: 30000,
            batchingEnabled: false
          })).then(function (_this2$pulsarClient$c) {
            _this2.pulsarProducer = _this2$pulsarClient$c;
          });
        }
      }();

      return Promise.resolve(_temp && _temp.then ? _temp.then(_temp2) : _temp2(_temp));
    } catch (e) {
      return Promise.reject(e);
    }
  }

}

exports.Client = Client;
//# sourceMappingURL=client.cjs.development.js.map
