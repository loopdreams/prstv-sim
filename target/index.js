import * as i0 from "react";
import * as i1 from "react-dom";
import * as i2 from "chart.js";

const ALL = {};

globalThis.shadow$bridge = function(name) {
  const ret = ALL[name];
  if (ret == undefined) {
    throw new Error("Dependency: " + name + " not provided by external JS!");
  } else {
    return ret;
  }
};

ALL["react"] = i0;

ALL["react-dom"] = i1;

ALL["chart.js"] = i2;
