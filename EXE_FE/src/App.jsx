import Header from "./components/Header";
import Hero from "./components/Hero";
import Products from "./pages/Products";
import Blog from "./pages/Blog";
import Policy from "./pages/Policy";
import Contact from "./pages/Contact";
import Footer from "./components/Footer";
import Notification from "./components/Notification";
import ChatBubble from "./components/ChatBubble";
import { useUi } from "./context/UiContext";
import AuthModal from "./modals/AuthModal";
import HomeBlogPreview from "./components/HomeBlogPreview";
import HomePolicySnippet from "./components/HomePolicySnippet";
import SubscribeForm from "./components/SubscribeForm";
import Cart from "./pages/Cart";

export default function App() {
  const { currentSection, setCurrentSection, showAuth, setShowAuth } = useUi();
  const showProducts =
    currentSection === "home" || currentSection === "products";

  return (
    <>
      {/* ✅ Truyền hàm điều hướng xuống Header */}
      <Header onNavigate={(section) => setCurrentSection(section)} />

      {currentSection === "home" && <Hero />}
      <main className="main-content">
        {showProducts && (
          <>
            <Products />
            {currentSection === "home" && (
              <>
                <HomeBlogPreview />
                <HomePolicySnippet />
                <SubscribeForm />
              </>
            )}
          </>
        )}
        {currentSection === "blog" && <Blog />}
        {currentSection === "policy" && <Policy />}
        {currentSection === "contact" && <Contact />}
        {currentSection === "cart" && <Cart />}
      </main>

      <Footer />
      <Notification />
      <ChatBubble />
      {showAuth && <AuthModal onClose={() => setShowAuth(false)} />}
    </>
  );
}
