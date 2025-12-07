part of 'package:barcode_inventory_app/main.dart';

class ScanScreen extends StatefulWidget {
  const ScanScreen({super.key});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> {
  String? _barcode;
  bool _torchOn = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('مسح الباركود'),
        actions: [
          IconButton(
            icon: Icon(_torchOn ? Icons.flash_off : Icons.flash_on),
            onPressed: () {
              setState(() {
                _torchOn = !_torchOn;
              });
            },
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            flex: 3,
            child: MobileScanner(
              allowDuplicates: false,
              torchEnabled: _torchOn,
              onDetect: (capture) {
                final code = capture.barcodes.first.rawValue;
                if (code != null && mounted) {
                  setState(() => _barcode = code);
                  HapticFeedback.mediumImpact();
                  Navigator.of(context).pop(code);
                }
              },
            ),
          ),
          Expanded(
            child: Center(
              child: Text(
                _barcode == null
                    ? 'وجه الكاميرا نحو الباركود'
                    : 'تم التقاط: $_barcode',
                style: Theme.of(context).textTheme.titleMedium,
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
