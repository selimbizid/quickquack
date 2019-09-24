import { QuickquackWebPage } from './app.po';

describe('quickquack-web App', () => {
  let page: QuickquackWebPage;

  beforeEach(() => {
    page = new QuickquackWebPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
