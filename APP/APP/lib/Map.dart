import 'package:flutter/material.dart';

class MapShow extends StatefulWidget {
  const MapShow({super.key});

  @override
  State<MapShow> createState() => _MapShowState();
}

class _MapShowState extends State<MapShow> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('MapShow'),
        centerTitle: true,
        backgroundColor: Colors.grey[800],
      ),
      body: SafeArea(
          child: Column(
            children: <Widget>[
              ElevatedButton.icon(
                  onPressed: (){Navigator.pushNamed(context, '/profile');},
                  label: Text('ProfilePage'),
                  icon: Icon(Icons.perm_identity_outlined,color: Colors.white,),
                  style: ButtonStyle(
                    backgroundColor: MaterialStatePropertyAll<Color>(Colors.amber),
                  ),
              )
            ],
          )
      ),
          bottomNavigationBar:BottomNavigationBar(
            items: const <BottomNavigationBarItem>[
            BottomNavigationBarItem(
            icon: Icon(Icons.map), // 地图图标
            label: 'Map',
            ),
            BottomNavigationBarItem(
            icon: Icon(Icons.person), // 个人资料图标
            label: 'Profile',
            ),
          ]
        )
      );
    }
  }
