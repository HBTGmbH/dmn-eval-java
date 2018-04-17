function unwrapInput(wrappedInput) {
    if (wrappedInput.type === 'plain') {
        return wrappedInput.value;
    }
    if (wrappedInput.type === 'function-1') {
        return function result(arg1) {
            return wrappedInput.value(arg1);
        };
    }
    if (wrappedInput.type === 'function-2') {
        return function result(arg1, arg2) {
            return wrappedInput.value(arg1, arg2);
        };
    }
    if (wrappedInput.type === 'function-3') {
        return function result(arg1, arg2, arg3) {
            return wrappedInput.value(arg1, arg2, arg3);
        };
    }
    if (wrappedInput.type === 'function-4') {
        return function result(arg1, arg2, arg3, arg4) {
            return wrappedInput.value(arg1, arg2, arg3, arg4);
        };
    }
    if (wrappedInput.type === 'function-5') {
        return function result(arg1, arg2, arg3, arg4, arg5) {
            return wrappedInput.value(arg1, arg2, arg3, arg4, arg5);
        };
    }
    if (wrappedInput.type === 'map') {
        var unwrappedInput = {};
        wrappedInput.properties.forEach(function (property) {
            unwrappedInput[property] = unwrapInput(wrappedInput.value[property]);
        });
        return unwrappedInput;
    }
    throw new Error('Unsupported wrapped input type: ' + wrappedInput.type);
}

function newDmnEvalJsWrapper(dmnEvalJs) {
    return new Packages.de.hbt.dmn_eval_java.impl.DmnEvalJsWrapper() {
        parseDmnXml: dmnEvalJs.decisionTable.parseDmnXml,
        evaluateDecision: function(decisionId, parsedDecision, wrappedInput) {
            var input = unwrapInput(wrappedInput);
            return dmnEvalJs.decisionTable.evaluateDecision(decisionId, parsedDecision, input);
        }
    };
}
