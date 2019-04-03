import Vizceral from 'vizceral';
const canvas = document.getElementById('vizceral');
canvas.width = canvas.height = 500;
const viz = new Vizceral(canvas);

viz.updateData({
  "renderer": "region",
  "name": "quickstart.clients-client1",
  "entryNode": "quickstart.clients-client1",
  "maxVolume": 0.0,
  "class": "normal",
  "updated": 1536240246639000,
  "nodes": [
    {
      "renderer": "",
      "name": "quickstart.services.helloservices-helloservice-d3875b6f-6069-4355-a05f-eb376c76abdc",
      "entryNode": "",
      "maxVolume": 18.0,
      "class": "normal",
      "updated": 1536240017785000,
      "nodes": [],
      "connections": [],
      "displayName": "quickstart.services.helloservices-helloservice-d3875b6f-6069-4355-a05f-eb376c76abdc",
      "metadata": []
    },
    {
      "renderer": "",
      "name": "quickstart.services.helloservices-helloservice-95613c6a-bb6d-4e39-a047-8d9624234838",
      "entryNode": "",
      "maxVolume": 284.0,
      "class": "normal",
      "updated": 1536239980285000,
      "nodes": [],
      "connections": [],
      "displayName": "quickstart.services.helloservices-helloservice-95613c6a-bb6d-4e39-a047-8d9624234838",
      "metadata": []
    },
    {
      "renderer": "",
      "name": "quickstart.clients-client1",
      "entryNode": "",
      "maxVolume": 0.0,
      "class": "normal",
      "updated": 1536240246639000,
      "nodes": [],
      "connections": [],
      "displayName": "quickstart.clients-client1",
      "metadata": []
    },
    {
      "renderer": "",
      "name": "quickstart.services.helloservices-helloservice-7182b26f-d18f-475a-8700-fac9a08644cb",
      "entryNode": "",
      "maxVolume": 409.0,
      "class": "normal",
      "updated": 1536239656231000,
      "nodes": [],
      "connections": [],
      "displayName": "quickstart.services.helloservices-helloservice-7182b26f-d18f-475a-8700-fac9a08644cb",
      "metadata": []
    },
    {
      "renderer": "",
      "name": "quickstart.services.helloservices-helloservice-9facf5e5-3529-437d-8a8c-bae4edd89b80",
      "entryNode": "",
      "maxVolume": 4.0,
      "class": "normal",
      "updated": 1536240246639000,
      "nodes": [],
      "connections": [],
      "displayName": "quickstart.services.helloservices-helloservice-9facf5e5-3529-437d-8a8c-bae4edd89b80",
      "metadata": []
    }
  ],
  "connections": [
    {
      "source": "quickstart.clients-client1",
      "target": "quickstart.services.helloservices-helloservice-95613c6a-bb6d-4e39-a047-8d9624234838",
      "metrics": {
        "danger": 0.0,
        "normal": 1.0,
        "warning": 0.0
      },
      "notices": [
        {
          "severity": 1,
          "title": "io.netifi.proteus.quickstart.service.HelloService",
          "link": ""
        }
      ],
      "updated": 1536239980285000
    },
    {
      "source": "quickstart.clients-client1",
      "target": "quickstart.services.helloservices-helloservice-9facf5e5-3529-437d-8a8c-bae4edd89b80",
      "metrics": {
        "danger": 0.0,
        "normal": 1.0,
        "warning": 0.0
      },
      "notices": [
        {
          "severity": 1,
          "title": "io.netifi.proteus.quickstart.service.HelloService",
          "link": ""
        }
      ],
      "updated": 1536240246639000
    },
    {
      "source": "quickstart.clients-client1",
      "target": "quickstart.services.helloservices-helloservice-7182b26f-d18f-475a-8700-fac9a08644cb",
      "metrics": {
        "danger": 0.0,
        "normal": 1.0,
        "warning": 0.0
      },
      "notices": [
        {
          "severity": 1,
          "title": "io.netifi.proteus.quickstart.service.HelloService",
          "link": ""
        }
      ],
      "updated": 1536239656231000
    },
    {
      "source": "quickstart.clients-client1",
      "target": "quickstart.services.helloservices-helloservice-d3875b6f-6069-4355-a05f-eb376c76abdc",
      "metrics": {
        "danger": 0.0,
        "normal": 1.0,
        "warning": 0.0
      },
      "notices": [
        {
          "severity": 1,
          "title": "io.netifi.proteus.quickstart.service.HelloService",
          "link": ""
        }
      ],
      "updated": 1536240017785000
    }
  ],
  "displayName": "quickstart.clients-client1",
  "metadata": []
});
viz.setView();
viz.animate();