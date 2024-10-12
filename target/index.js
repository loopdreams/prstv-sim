import * as i1 from "react-dom";
import * as i0 from "react";
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

ALL["react-dom"] = {
  findDOMNode: i1.findDOMNode,
  render: i1.render,
  unmountComponentAtNode: i1.unmountComponentAtNode
};

ALL["react"] = {
  Children: i0.Children,
  useRef: i0.useRef,
  createElement: i0.createElement,
  Fragment: i0.Fragment,
  Component: i0.Component,
  useEffect: i0.useEffect,
  useState: i0.useState,
  memo: i0.memo
};

ALL["chart.js"] = {
  Chart: i2.Chart
};
