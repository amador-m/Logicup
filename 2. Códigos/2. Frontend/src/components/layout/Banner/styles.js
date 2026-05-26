import styled from "styled-components";

export const Container = styled.section`
  min-height: 620px;
  padding: 120px;
  position: relative;
  background-image: url("https://images.unsplash.com/photo-1515879218367-8466d910aaa4?q=80&w=1600");
  background-size: cover;
  background-position: center;
`;

export const Overlay = styled.div`
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(4, 4, 35, 0.94),
    rgba(118, 73, 53, 0.78),
    rgba(227, 92, 51, 0.35)
  );
`;

export const Content = styled.div`
  max-width: 720px;
  position: relative;
  z-index: 2;
`;

export const Tag = styled.span`
  color: var(--yellow);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
`;

export const Title = styled.h2`
  color: var(--cream);
  font-size: 58px;
  line-height: 1.1;
  margin: 18px 0;
`;

export const Text = styled.p`
  color: var(--cream);
  font-size: 18px;
  line-height: 1.7;
  margin-bottom: 32px;
`;

export const Buttons = styled.div`
  display: flex;
  gap: 18px;
`;

export const PrimaryButton = styled.button`
  background-color: var(--yellow);
  color: var(--dark);
  border: none;
  padding: 15px 34px;
  border-radius: 30px;
  font-weight: 700;
  cursor: pointer;
`;

export const SecondaryButton = styled.button`
  background-color: transparent;
  color: var(--cream);
  border: 2px solid var(--cream);
  padding: 13px 34px;
  border-radius: 30px;
  font-weight: 700;
  cursor: pointer;
`;