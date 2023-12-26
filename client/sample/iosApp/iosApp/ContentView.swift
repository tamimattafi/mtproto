import SwiftUI
import shared

struct ContentView: View {
	let greet = Greeting().greet()

	var body: some View {
		Text(greet)
        Button("Connect") {
            print("clicked")
            ConnectionManager().start()
        }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
