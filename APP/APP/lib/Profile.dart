import 'package:flutter/material.dart';

class Profile extends StatefulWidget {
  const Profile({super.key});

  @override
  State<Profile> createState() => _ProfileState();
}

class _ProfileState extends State<Profile> {
  void getData() {
    //simutale delat ! only for debug
    Future.delayed(Duration(seconds: 3), () {
      print('ParoX');
    });

    print('loading...');
  }

  @override
  void initState() {
    super.initState();
    getData();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
      title: Text('Profile'),
      backgroundColor: Colors.grey[800],
    ));
  }
}
