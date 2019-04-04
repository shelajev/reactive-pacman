import Vizceral from 'vizceral';
const canvas = document.getElementById('vizceral');
const viz = new Vizceral(canvas);

viz.setOptions({
  showLabels: true,
  allowDraggingOfNodes: true
});

// viz.updateStyles({
//   colorText: 'rgb(214, 214, 214)',
//   colorTextDisabled: 'rgb(129, 129, 129)',
//   colorTraffic: {
//     normal: 'rgb(186, 213, 237)',
//     normalDonut: 'rgb(91, 91, 91)',
//     warning: 'rgb(268, 185, 73)',
//     danger: 'rgb(184, 36, 36)',
//   },
//   colorNormalDimmed: 'rgb(101, 117, 128)',
//   colorBackgroundDark: 'rgb(35, 35, 35)',
//   colorLabelBorder: 'rgb(16, 17, 18)',
//   colorLabelText: 'rgb(0, 0, 0)',
//   colorDonutInternalColor: 'rgb(35, 35, 35)',
//   colorDonutInternalColorHighlighted: 'rgb(255, 255, 255)',
//   colorConnectionLine: 'rgb(91, 91, 91)',
//   colorPageBackground: 'rgb(45, 45, 45)',
//   colorPageBackgroundTransparent: 'rgba(45, 45, 45, 0)',
//   colorBorderLines: 'rgb(137, 137, 137)',
//   colorArcBackground: 'rgb(60, 60, 60)'
// });

// viz.updateDefinitions({
//   volume: {
//     default: {
//       top: { header: '% RPS', data: 'data.volumePercent', format: '0.00%' },
//       bottom: { header: 'ERROR RATE', data: 'data.classPercents.danger', format: '0.00%' },
//       donut: {},
//       arc: {}
//     },
//     region: {
//       top: { header: 'SERVICE RPS', data: 'data.volume', format: '0.0' }
//     },
//     entry: {
//       top: { header: 'TOTAL RPS', data: 'data.volume', format: '0.0' }
//     }
//   }
// });

viz.updateData({
  renderer: 'global',
  name: 'edge',
  entryNode: 'INTERNET',
  layout: 'dns',
  nodes: [
    {
      name: 'INTERNET',
      class: 'normal'
    },
    {
      renderer: 'region',
      name: 'us-east-1',
      maxVolume: 50000,
      class: 'normal',
      updated: 1466838546805,
      nodes: [
        {
          renderer: 'region',
          name: 'dummy-1'
        },
        {
          renderer: 'region',
          name: 'dummy-3'
        }
      ],
      connections: [
        {
          source: 'dummy-1',
          target: 'us-east-2',
          metrics: {
            normal: 10000,
            danger: 107,
            warning: 0
          },
          notices: [
          ],
          class: 'normal'
        },
        {
          source: 'dummy-1',
          target: 'dummy-3',
          metrics: {
            normal: 2000,
            danger: 200,
            warning: 0
          },
          notices: [
          ],
          class: 'normal'
        }
      ]
    },
    {
      renderer: 'region',
      name: 'us-east-2',
      maxVolume: 50000,
      class: 'normal',
      updated: 1466838546805,
      nodes: [
        {
          renderer: 'region',
          name: 'dummy-2'
        }
      ],
      connections: [{
        source: 'dummy-2',
        target: 'us-east-1',
        metrics: {
          normal: 10000,
          danger: 107,
          warning: 0
        },
        notices: [
        ],
        class: 'normal'
      }]
    }
  ],
  connections: [
    {
      source: 'us-east-1',
      target: 'us-east-2',
      metrics: {
        normal: 10000,
        danger: 107,
        warning: 0
      },
      notices: [],
      class: 'normal'
    },
    {
      source: 'us-east-2',
      target: 'us-east-1',
      metrics: {
        normal: 10000,
        danger: 107,
        warning: 0
      },
      notices: [],
      class: 'normal'
    }
  ]
});
viz.setView();
viz.animate();