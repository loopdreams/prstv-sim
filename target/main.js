import { Chart, BarController, BarElement, LinearScale, CategoryScale, Tooltip } from 'chart.js'
import annotationPlugin from 'chartjs-plugin-annotation';
import {SankeyController, Flow} from 'chartjs-chart-sankey';


Chart.register(annotationPlugin, SankeyController, Flow, BarController, BarElement, LinearScale, CategoryScale, Tooltip);
